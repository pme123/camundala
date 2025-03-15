package camundala.gateway

import sttp.tapir.Schema.annotations.description

@description("Contains information about a process instance or execution")
case class ProcessInfo(
    @description("ID of the process instance")
    processInstanceId: String,
    
    @description("Optional business key associated with the process")
    businessKey: Option[String] = None,
    
    @description("Current status of the process")
    status: ProcessStatus = ProcessStatus.Active
)

@description("Status of a process instance")
enum ProcessStatus:
  case Active, Completed, Failed
