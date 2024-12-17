package camundala.helper.dev.update

case class ApiGenerator()(using config: DevConfig):

  lazy val generate: Unit =
    createIfNotExists(
      config.projectDir / ModuleConfig.apiModule.packagePath(
        config.projectPath
      ) / "ApiProjectCreator.scala",
      api
    )
    createOrUpdate(config.projectDir / "03-api" / "OpenApi.html", openApiHtml)
    createOrUpdate(config.projectDir / "03-api" / "PostmanOpenApi.html", postmanOpenApiHtml)
  end generate

  lazy val api =
    s"""package ${config.projectPackage}
       |package api
       |
       |import bpmn.*
       |
       |object ApiProjectCreator extends CompanyApiCreator:
       |
       |  lazy val projectName: String = "${config.projectName}"
       |
       |  val title = "${config.projectClassName}"
       |
       |  lazy val projectDescr =
       |    "TODO Your Project description."
       |
       |  val version = "0.1.0-SNAPSHOT"
       |
       |  document(
       |    myProcessApi,
       |    //..
       |  )
       |
       |  private lazy val myProcessApi =
       |    import myProcess.v1.*
       |    api(MyProcess.example)(
       |      // userTasks / workers etc.
       |    )
       |end ApiProjectCreator
       |""".stripMargin
  end api
  private lazy val openApiHtml =
    s"""<!-- $helperDoNotAdjustText -->
       |<!DOCTYPE html>
       |<html>
       |<head>
       |    <title>ReDoc</title>
       |    <!-- needed for adaptive design -->
       |    <meta charset="utf-8"/>
       |    <meta name="viewport" content="width=device-width, initial-scale=1">
       |    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
       |
       |    <!-- bpmn styles -->
       |    <link rel="stylesheet" href="https://unpkg.com/bpmn-js@11.5.0/dist/assets/bpmn-js.css">
       |    <!-- dmn styles -->
       |    <link rel="stylesheet" href="https://unpkg.com/dmn-js@14.1.0/dist/assets/dmn-js-shared.css">
       |    <link rel="stylesheet" href="https://unpkg.com/dmn-js@14.1.0/dist/assets/dmn-js-drd.css">
       |    <link rel="stylesheet" href="https://unpkg.com/dmn-js@14.1.0/dist/assets/dmn-js-decision-table.css">
       |    <link rel="stylesheet" href="https://unpkg.com/dmn-js@14.1.0/dist/assets/dmn-js-literal-expression.css">
       |    <link rel="stylesheet" href="https://unpkg.com/dmn-js@14.1.0/dist/assets/dmn-font/css/dmn.css">
       |
       |    <!--
       |    ReDoc doesn't change outer page styles
       |    -->
       |    <style>
       |        body {
       |            margin: 0;
       |            padding: 0;
       |            z-index: 10;
       |
       |        }
       |        header {
       |            background-color:  #ebf6f7;
       |            border-bottom: 1px solid #a7d4de;
       |            z-index: 12;
       |        }
       |        .homeLink {
       |            text-decoration: none;
       |            margin: 0;
       |            padding-top: 8px;
       |            padding-bottom: 0px;
       |            padding-left: 16px;
       |            width: 100%;
       |            text-align: center;
       |
       |        }
       |        /* The sticky class is added to the header with JS when it reaches its scroll position */
       |        .sticky {
       |            position: fixed;
       |            top: 0;
       |            width: 100%
       |        }
       |        .sticky + .content {
       |            padding-top: 102px;
       |        }
       |        .diagramCanvas {
       |            border: solid 1px grey;
       |            height:500px;
       |        }
       |        .diagram {
       |            padding: 5px;
       |            height: 100%;
       |        }
       |    </style>
       |    <script>
       |        function downloadSVG(id) {
       |            const container = document.getElementById(id);
       |            const svg = container.getElementsByTagName('svg')[1];
       |            console.log(svg)
       |            svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg')
       |            const blob = new Blob([svg.outerHTML.toString()]);
       |            const element = document.createElement("a");
       |            element.download = id + ".svg";
       |            element.href = window.URL.createObjectURL(blob);
       |            element.click();
       |            element.remove();
       |        }
       |    </script>
       |</head>
       |<body>
       |<header id="myHeader">
       |    <p class="homeLink">
       |        <a href="../index.html">
       |            <svg id="Layer_1" data-name="Layer 1" xmlns="http://www.w3.org/2000/svg" width="24px" height="24px"
       |                 viewBox="0 0 391.08 391.08">
       |                <title>Valiant Documentation Home</title>
       |                <defs>
       |                    <style>.cls-1 {
       |                        fill: #007c99;
       |                        stroke: #007c99;
       |                        stroke-miterlimit: 10;
       |                        stroke-width: 3px;
       |                    }
       |
       |                    .cls-2 {
       |                        fill: #007c99;
       |                    }</style>
       |                </defs>
       |                <title>Icon_home_remixofdynamitt</title>
       |                <g id="layer1">
       |                    <path id="rect2391" class="cls-2"
       |                          d="M326.67,203.55L200.38,91.71,74,203.6V363.47a7.44,7.44,0,0,0,7.46,7.45h79v-70.1a7.44,7.44,0,0,1,7.45-7.46h64.88a7.44,7.44,0,0,1,7.45,7.46v70.1h79a7.42,7.42,0,0,0,7.45-7.45V203.55Z"
       |                          transform="translate(-4.8 -5.17)"/>
       |                    <path id="path2399" class="cls-2"
       |                          d="M199.65,30.51L20.44,189.19l18.88,21.29L200.38,67.86l161,142.62,18.84-21.29L201.08,30.51l-0.7.81-0.73-.81h0Z"
       |                          transform="translate(-4.8 -5.17)"/>
       |                    <path id="rect2404" class="cls-2" d="M74,53.35h45.43l-0.4,26.91L74,120.94V53.35h0Z"
       |                          transform="translate(-4.8 -5.17)"/>
       |                </g>
       |            </svg>
       |        </a>
       |    </p>
       |</header>
       |
       |<script>
       |    // When the user scrolls the page, execute myFunction
       |    window.onscroll = function() {myFunction()};
       |
       |    // Get the header
       |    var header = document.getElementById("myHeader");
       |
       |    // Get the offset position of the navbar
       |    var sticky = header.offsetTop;
       |
       |    // Add the sticky class to the header when you reach its scroll position. Remove "sticky" when you leave the scroll position
       |    function myFunction() {
       |        if (window.scrollY > sticky) {
       |            header.classList.add("sticky");
       |        } else {
       |            header.classList.remove("sticky");
       |        }
       |    }
       |</script>
       |<!-- bpmn viewer -->
       |<script src="https://unpkg.com/bpmn-js@11.5.0/dist/bpmn-viewer.development.js"></script>
       |<!-- dmn viewer -->
       |<script src="https://unpkg.com/dmn-js@14.1.0/dist/dmn-viewer.development.js"></script>
       |<!-- jquery (required for bpmn / dmn example) -->
       |<script src="https://unpkg.com/jquery@3.3.1/dist/jquery.js"></script>
       |<script>
       |
       |    function openFromUrl(url, viewer) {
       |        console.log('attempting to open <' + url + '>');
       |        $$.ajax("src/main/resources/camunda/" + url, {dataType: 'text'}).done(async function (xml) {
       |
       |            try {
       |                await viewer.importXML(xml);
       |                if(url.endsWith(".bpmn"))
       |                    viewer.get('canvas').zoom('fit-viewport');
       |                else {
       |                    const activeEditor = viewer.getActiveViewer();
       |                    activeEditor.get('canvas').zoom('fit-viewport');
       |                }            } catch (err) {
       |                console.error(err);
       |            }
       |        });
       |    }
       |</script>
       |<redoc class="content" spec-url='./openApi.yml'></redoc>
       |<script src="https://cdn.jsdelivr.net/npm/redoc@next/bundles/redoc.standalone.js"></script>
       |</body>
       |</html>
       |""".stripMargin

  private lazy val postmanOpenApiHtml =
    s"""<!-- $helperDoNotAdjustText -->
       |<!DOCTYPE html>
       |<html>
       |<head>
       |    <title>ReDoc</title>
       |    <!-- needed for adaptive design -->
       |    <meta charset="utf-8"/>
       |    <meta name="viewport" content="width=device-width, initial-scale=1">
       |    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
       |
       |    <!--
       |    ReDoc doesn't change outer page styles
       |    -->
       |    <style>
       |        body {
       |            margin: 0;
       |            padding: 0;
       |        }
       |    </style>
       |</head>
       |<body>
       |<redoc spec-url='./postmanOpenApi.yml'></redoc>
       |<script src="https://cdn.jsdelivr.net/npm/redoc@next/bundles/redoc.standalone.js"> </script>
       |</body>
       |</html>""".stripMargin
end ApiGenerator
