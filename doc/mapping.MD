# Mapping
Mapping is one of the main work you have to do when you work with BPMN.

Camundala tries to support the main use cases:

## Input Mapping
### String from a Json-Path
`def inputStringFromJsonPath(key: PropKey, path: JsonPath): InputOutputExpression`

**Example**: 

    BusinessRuleTask("CountryRiskTask")
        .dmn("country-risk.dmn", "approvalRequired")
        .inputStringFromJsonPath("currentCountry", Seq("existingAddress", "countryIso"))
        .inputStringFromJsonPath("targetCountry", Seq("newAddress", "countryIso"))
        
This creates a Business Rule Task with 2 String input variables. 
They expect 2 Jsons in the Bag. e.g. `newAddress`:

    {
      "street": "Murtengasse 22",
      "zipCode": "3600",
      "city": "Thun",
      "countryIso": "CH"
    }
    
### String Variables from JSON for a Form
`def inputFromJson(key: PropKey, generatedForm: GeneratedForm): Seq[InputOutputExpression]`

If you follow naming conventions you can define your data structures with `GeneratedForms`.
Let's say you have the JSON from above (`newAddress`). This gives you a Form like:

    val myForm = ...
    GroupField("newAddressGroup")
      .---(textField("newAddress__street"))
      .---(RowGroupField(s"newAddressCityCountry")
        .---(textField("newAddress__zipCode"))
        .---(textField("newAddress__city"))
        .---(textField("newAddress__countryIso"))
      )
      ...
      
With the convention `jsonValueKey__jsonFieldValue` we can now create the Input mapping:

`inputFromJson("newAddress", myForm)` creates now the mappings for all these Form fields, e.g.
`newAddress__street -> ${S(existingAddress).prop("street")}`

### String Variables from Map for a Form
`def inputFromMap(key: PropKey, generatedForm: GeneratedForm): Seq[InputOutputExpression]`

This is the same as with the JSON, but now it expects a map, where the fieldNames are the keys of the map, like:

    newAddress = {"street" -> "Murtengasse 22", ...}
      
With the convention `jsonValueKey__jsonFieldValue` we can now create the Input mapping:

`inputFromMap("newAddress", `myForm`)` creates now the mappings for all these Form fields, e.g.
`newAddress__street -> ${S(existingAddress).prop("street")}`

## Output Mapping
### Json Variable from a Form
`def outputToJson(key: PropKey, generatedForm: GeneratedForm): InputOutputExpression`

Like the for inputs, with the same name convention, it is possible to create a JSON object from a Form.

`.outputToJson(newAddress, myForm) from the example above creates an Output Mapping for `newAddress`:

    import groovy.json.JsonOutput
    import static org.camunda.spin.Spin.*
    
    result = JsonOutput.toJson(["street" : "newAddress__street",
    "zipCode" : "newAddress__zipCode",
    "city" : "newAddress__city",
    "countryIso" : "newAddress__countryIso"])
    
    S(result)

### Map Variable from a Form
`def outputToMap(key: PropKey, form: GeneratedForm): InputOutputMap`

Analog you can create a Map from a Form.

`.outputToMap(newAddress, myForm) from the example above creates an Output Mapping for `newAddress`:

    Map("street" -> "Murtengasse 22", ...)
