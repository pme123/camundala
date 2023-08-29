package camundala
package camunda7.worker

import domain.*
import bpmn.*
import CamundalaWorkerError.*
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.`type`.{PrimitiveValueType, ValueType}
import org.camunda.bpm.engine.variable.value.TypedValue


import scala.language.implicitConversions

trait CamundaHelper:

  def variableTypedOpt(
      varKey: String | InputParams
  ): HelperContext[Option[TypedValue]] =
    Option(summon[ExternalTask].getVariableTyped(varKey.toString))

  /** Returns the Variable in the Bag. If there is none it return `null`. It
    * returns whatever datatype the variable contains. Usage: myVar =
    * variableOpt("myVar") println("Say: $myVar")
    */
  def variableOpt[A: Decoder](
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, Option[A]]] =
    for {
      maybeJson <- jsonVariableOpt(varKey)
      obj <- maybeJson
        .map(_.as[Option[A]])
        .getOrElse(Right(None))
        .left
        .map(err =>
          BadVariableError(
            s"Problem decoding Json to ${nameOfType[A]}: ${err.getMessage}"
          )
        )
    } yield obj

  def jsonVariableOpt(
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, Option[Json]]] =
    variableTypedOpt(varKey)
      .map {
        case typedValue if typedValue.getType == ValueType.NULL =>
          Right(None) // k -> null as Camunda Expressions need them
        case typedValue =>
          extractValue(typedValue)
            .map(v => Some(v))
      }
      .getOrElse(Right(None))

  import camundala.bpmn.*
  import camundala.domain.*

  // used for input variables you can define with Array of Strings or a comma-separated String
  // if not set it returns an empty Seq
  def extractSeqFromArrayOrString(
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, Seq[String]]] =
    jsonVariableOpt(varKey)
      .flatMap {
        case Some(value) if value.isArray =>
          value
            .as[Seq[String]]
            .map(_.filter(_.trim.nonEmpty))
            .left
            .map { error =>
              error.printStackTrace()
              BadVariableError(
                s"Could not extract Seq for an Array or comma-separated String: ${error.getMessage}"
              )
            }
        case Some(value) if value.isString =>
          value
            .as[String]
            .map(_.split(",").toSeq.filter(_.trim.nonEmpty))
            .left
            .map { error =>
              error.printStackTrace()
              BadVariableError(
                s"Could not extract Seq for an Array or comma-separated String: ${error.getMessage}"
              )
            }
        case _ =>
          Right(Seq.empty[String])
      }

  /** Analog `variable(String vari)`. You can define a Value that is returned if
    * there is no Variable with this name. Usage: myVar = variable("myVar",
    * "hello") println("Say: $myVar")
    */
  def variable[A: Decoder](
      varKey: String | InputParams,
      defaultObj: A
  ): HelperContext[Either[BadVariableError, A]] =
    variableOpt[A](varKey).map(_.getOrElse(defaultObj))

  /** Returns the Variable in the Bag. B if there is no Variable with that
    * identifier. Usage: myVar = variable[String]("myVar") println("Say:
    * $myVar")
    */
  def variable[T: Decoder](
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, T]] =
    variableOpt(varKey)
      .flatMap(
        _.toEither(
          s"The Variable '$varKey' is required! But does  not exist in your Process"
        )
      )
  end variable

  def topicName: HelperContext[String] =
    summon[ExternalTask].getTopicName

  extension [T](option: Option[T])

    def toEither(msg: String): HelperContext[Either[BadVariableError, T]] =
      toEither(BadVariableError(errorMsg = msg))

    def toEither[E <: CamundalaWorkerError](
        error: E
    ): HelperContext[Either[E, T]] =
      option
        .map(Right(_))
        .getOrElse(
          Left(error)
        )

  end extension // Option

  /** Returns a Query String from all the Variables found in the Bag.
    * @param varKeys
    *   a list with Variable names you want query parameters.
    *
    * Usage: myVars = variablesAsQueryParams("myVar", "otherVar", "optionalVar")
    * myURL = "http://server.ch?$myVars" // myVars=okidoki&otherVar=OK
    */
  def variablesAsQueryParams(
      varKeys: String*
  ): HelperContext[Seq[(String, Seq[String])]] =
    varKeys
      .map(key =>
        key -> Option(summon[ExternalTask].getVariable(key))
          .asInstanceOf[Option[Any]]
      )
      .collect { case k -> Some(value) =>
        k -> Seq(s"$value")
      }

  def extractValue(typedValue: TypedValue): Either[BadVariableError, Json] =
    //println(s"typedValue.getType: ${typedValue.getType}")
    typedValue.getType match
      case pt: PrimitiveValueType if pt.getName == "json" =>
        val jsonStr = typedValue.getValue.toString
        parser
          .parse(jsonStr)
          .left
          .map(ex => BadVariableError(s"Input is not valid: $ex"))

      case _: PrimitiveValueType =>
        typedValue.getValue match
          case vt: DmnValueSimple =>
            Right(vt.asJson)
          case en: scala.reflect.Enum =>
            Right(Json.fromString(en.toString))
          case other =>
            Left(
              BadVariableError(
                s"Input is not valid: Unexpected PrimitiveValueType: $other"
              )
            )

      case other =>
        Left(
          BadVariableError(
            s"Unexpected ValueType ${other.getName} - but is ${typedValue.getType}"
          )
        )

  end extractValue
end CamundaHelper

/*
    /**
 * Creates a Json Object for a Variable in the Bag and returns it as a String.
 * @throws NoSuchElementException if there is no Json Variable with that identifier.
 * Usage:
 *     myJson = stringJsonVariable("myJson")
 *     myNewJson = """{ "myJson": $myJson }"""
 */
    @throws[NoSuchElementException]
    def stringJsonVariable(variable: String): String =
        val variableTyped: TypedValue = externalTask.getVariableTyped(variable)
        if (variableTyped == null)
            throw new NoSuchElementException("Json Variable '$variable' was not set!")
        variableTyped.getValue().toString()


    /**
 * Creates a Json Object for a Variable in the Bag and returns it as a String.
 * If it does not have this variable it returns the `defaultVar`.
 * Usage:
 *     myJson = stringJsonVariable("myJson", "{}")
 *     myNewJson = """{ "myJson": $myJson }"""
 */
    def stringJsonVariable( variable: String, defaultVar: String): String =
        def variableTyped: TypedValue = externalTask.getVariableTyped(variable)
        if (variableTyped == null || variableTyped.getType() == ValueType.NULL)
            defaultVar
        else
            variableTyped.getValue().toString()


/**
 * Creates a Groovy Json Object for a Variable in the Bag.
 * @throws NoSuchElementException if there is no Json Variable with that identifier.
 * Usage:
 *     myJson = groovyJsonVariable("myJson")
 *     println("My Json: ${myJson.address.city}")
 */
Object groovyJsonVariable(String variable) {
    def jsonSlurper = new JsonSlurper()
    jsonSlurper.parseText(stringJsonVariable(variable))
}

/**
 * Creates a Groovy Json Object for a Variable in the Bag.
 * If it does not have this variable it returns the `defaultVar`.
 * Usage:
 *     myJson = groovyJsonVariable("myJson", [:])
 *     println("My Json: ${myJson.address.city}")
 */
Object groovyJsonVariable(String variable, Object defaultVar) {
    def jsonSlurper = new JsonSlurper()
    jsonSlurper.parseText(stringJsonVariable(variable, JsonOutput.toJson(defaultVar).toString()))
}

/**
 * Returns the Variable in the Bag.
 * If there is none it return `null`.
 * It returns whatever datatype the variable contains.
 * Usage:
 *     myVar = variable("myVar")
 *     println("Say: $myVar")
 */
def variable(vari: String): Any {
    externalTask.getVariable(vari)
}

/**
 * Analog `variable(String vari)`.
 * You can define a Value that is returned if there is no Variable with this name.
 * Usage:
 *     myVar = variable("myVar", "hello")
 *     println("Say: $myVar")
 */
String variable(String vari, String defaultStr) {
    variable(vari) ?: defaultStr
}

/**
 * Analog `variable(String vari)`.
 * You can define a Value that is returned if there is no Variable with this name.
 * Usage:
 *     myVar = variable("myVar", true)
 *     println("Is it ok: $myVar")
 */
Boolean variable(String vari, Boolean defaultBoolean) {
    if (variable(vari) == null) {
        defaultBoolean
    } else {
        (Boolean) variable(vari)
    }
}

/**
 * Analog `variable(String vari)`.
 * You can define a Value that is returned if there is no Variable with this name.
 * Usage:
 *     myVar = variable("myVar", 12L)
 *     println("The number is: $myVar")
 */
Long variable(String vari, Long defaultLong) {
    (Long) variable(vari) ?: defaultLong
}
/**
 * Analog `variable(String vari)`.
 * You can define a Value that is returned if there is no Variable with this name.
 * Usage:
 *     myVar = variable("myVar", 12.5)
 *     println("The number is: $myVar")
 */
Double variable(String vari, Double defaultDouble) {
    (Double) variable(vari) ?: defaultDouble
}
/**
 * Analog `variable(String vari)`.
 * You can define a Value that is returned if there is no Variable with this name.
 * Usage:
 *     myVar = variable("myVar", 12.5)
 *     println("The number is: $myVar")
 */
BigDecimal variable(String vari, BigDecimal defaultBigDecimal) {
    (BigDecimal) variable(vari) ?: defaultBigDecimal
}


/**
 * Analog `variable(String vari)`.
 * You can define a Value that is returned if there is no Variable with this name.
 * Usage:
 *     myVar = variable("initiatorGroups", ["KUBE", "DAZ", "QMS"])
 *     println("The List is: $myVar")
 */
List<Object> variable(String vari, List<Object> defaultList) {
    (List<Object>) variable(vari) ?: defaultList
}

/**
 * Returns the Variable in the Bag.
 * @throws NoSuchElementException if there is no Variable with that identifier.
 * Usage:
 *     myVar = variable("myVar")
 *     println("Say: $myVar")
 */
Object variableOrThrow(String vari) {
    def result = variable(vari)
    if (result == null)
        throw new NoSuchElementException("$vari was not set!")
    result
}

/**
 * Returns the Variable in the Bag as an optional query parameter.
 * If there is no such Variable in the Bag or it is empty > an empty String is returned.
 *
 *  Usage:
 *     myVar = variableAsQueryParam("myVar")
 *     myURL = "http://server.ch?$myVar" // myVars=okidoki
 */
String variableAsQueryParam(String vari) {
    keyValueAsQueryParam(vari, variable(vari))
}

/**
 * Returns a Query String from all the Variables found in the Bag.
 * @param vars a list with Variable names you want query parameters.
 *
 *  Usage:
 *     myVars = variablesAsQueryParams("myVar", "otherVar", "optionalVar")
 *     myURL = "http://server.ch?$myVars" // myVars=okidoki&otherVar=OK
 */
String variablesAsQueryParams(List<String> vars) {
    concatQueryParams(vars.collect { variableAsQueryParam(it) })
}

/**
 * Returns the key-value in the Bag as an optional query parameter.
 * If there is no such Variable in the Bag or it is empty > an empty String is returned.
 *
 *  Usage:
 *     myVar = keyValueAsQueryParam("myVar", "okidoki)
 *     myURL = "http://server.ch?$myVar" // "myVars=okidoki"
 *     myVar = keyValueAsQueryParam("myVar", "null)
 *     myURL = "http://server.ch?$myVar" // ""
 */
String keyValueAsQueryParam(String key, Object value) {
    def resultParam = ""
    if (value != null && !value.toString().trim().isEmpty())
        resultParam = "$key=${urlEncode(value.toString())}"
    resultParam.toString()
}

/**
 * Returns a list Map of key-value pairs as an optional query parameter.
 * If the value is null or it is empty > an empty String is returned.
 *
 *  Usage:
 *     myVar = keyValueMapAsQueryParams([ key : "value", key2 : null ])
 *     myURL = "http://server.ch?$myVar" // key=value
 */
String keyValueMapAsQueryParams(Map<String, String> keyValMap) {
    concatQueryParams(
            keyValMap.collect { keyValueAsQueryParam(it.getKey(), it.getValue()) }
    )
}

private String concatQueryParams(List<String> queryParams) {
    def filteredParams = queryParams.findAll { it.length() > 0 }
    def joinedParams = filteredParams.join('&')
    joinedParams
}

/**
 * Encodes the value to be URL conform.
 * e.g. 'Sonnenweg 23 A 6414 oberarth' -> 'Sonnenweg%2023%20A%206414%20oberarth'
 * if the value is null it returns also null.
 */
String urlEncode(String value) {
    value != null ?
            URLEncoder.encode(value, "UTF-8") :
            null
}

/**
 * Sets a Value - Groovy `Map`s and `List`s are converted to JSONs.
 * Everything else just calls `externalTask.setVariable(key, value)`
 */
void setVariable(String key, Object value) {
    if (value instanceof Map<Object, Object> || value instanceof List<Object>)
        externalTask.setVariable(key, S(JsonOutput.toJson(value)))
    else
        externalTask.setVariable(key, value)
}

/**
 * Sets a Value - Groovy `Map`s and `List`s as Groovy Objects.
 * Just calls `externalTask.setVariable(key, value)`
 */
void setGroovyVariable(String key, Object value) {
    externalTask.setVariable(key, value)
}

void debug(String msg) {
    logger().debug(logInfo() + msg)
}

void info(String msg) {
    logger().info(logInfo() + msg)
}

void warn(String msg) {
    logger().warn(logInfo() + msg)
}

void error(String msg) {
    logger().error(logInfo() + msg)
}

private Logger logger() {
    LoggerFactory.getLogger("bpf.GroovyLogger")
}

private String logInfo() {
    def processDefinitionKey = externalTask.getProcessDefinition().getKey()
    def processInstanceId = externalTask.getProcessInstanceId()
    def businessKey = externalTask.getBusinessKey()
    """
 - ProcessDefinitionKey: $processDefinitionKey
 - ProcessInstanceId:  $processInstanceId
 - BusinessKey:        $businessKey
"""
}

/**
 * If you have a Map you do not know what case the key has, use this one.
 *
 * Example:
 * headers = ["x-request": "ABC"]
 * This works with `headers.get("X-Request")` or `headers.get("x-request")`
 */
String mapValue(Map<String, Object> map, String mapKey) {
    Map<String, Object> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if(map == null)
        throw new RuntimeException("The function `mapValue` requires the parameter `map` - is null for $mapKey".toString())
    treeMap.putAll(map)
    treeMap.get(mapKey)
}

/**
 * Handles mocking in a standard way.
 * - If you want to mock > `mocked` must be true.
 * - You must provide a `defaultMock` that will be taken if there is no `customMock` defined.
 *
 * - it also sets the testMode
 * @param defaultMock if
 */
void handleMocking(String defaultMock) {
    // testMode
    setVariable("testMode",  variable("testMode", false))
    // mocking
    doMock = variable("mocked", false)
    // get variable customMock or outputMock
    customMock = groovyJsonVariable("customMock", groovyJsonVariable("outputMock", null))

    def mocked = doMock || customMock != null
    setVariable("mocked", mocked)
    if (mocked) {
        Map<String, Object> mockedResult = null

        if (customMock != null)
            mockedResult = customMock
        else {
            mockedResult = new JsonSlurper().parseText(defaultMock)
        }

        mockedResult.each {
            println("setVariable MOCKED: ${it.key} -> ${it.value}")
            setVariable(it.key, it.value) // the value is automatically translated to JSON
        }
    }
    // isServiceMock (by default it is a service mock - as in service process that is what the default is)
    isServiceMock = customMock == null || customMock.respStatus != null
    setVariable("isServiceMock", isServiceMock)
}
 */
