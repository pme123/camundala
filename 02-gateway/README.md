# Camundala Gateway

## Current Situation
Currently, we use REST Services to interact with Camunda (e.g., starting processes). 
However, our documentation cannot map these interactions one-to-one due to several challenges:

- Distinction between variables, businessKey, etc.
- Technical variables like syncTimeout
- Ambiguous paths

With our new ability to develop and deploy custom apps (Workers), we propose creating a dedicated REST API wrapper.

## Proposed Solution
A unified (REST) API Gateway that provides:

### Clear and Consistent Endpoints

#### Scala API
```scala
// process
def startProcess(processDefId: String, in: In): Out
def startProcessAsync(processDefId: String, in: In): ProcessInfo
def sendMessage(messageDefId: String, in: In): ProcessInfo
def sendSignal(signalDefId: String, in: In): ProcessInfo
// dmn
def executeDmn(dmnDefId: String, in: In): ProcessInfo
// worker
def startWorker(workerDefId: String, in: In): ProcessInfo
def registerTopic(topicName: String): Unit
```

#### Public REST Endpoints
```http
POST /process/{processDefId}     # Start process synchronously
POST /process/{processDefId}/async    # Start process asynchronously
POST /message/{messageDefId}          # Send message
POST /signal/{signalDefId}           # Send signal
POST /worker/{workerDefId}           # Start worker synchronously
POST /dmn/{dmnDefId}                 # Execute DMN synchronously
GET  /process/{processId}/userTask/{userTaskDefId}/variables  # Get current user task variables
POST /process/{processId}/userTask/{userTaskId}/complete      # Complete current user task
```

### Key Benefits

1. **Direct Postman Integration**: API documentation can be used directly in Postman
2. **Clean Input/Output**: Uses documented In/Out objects
3. **Simplified Headers**: Process/Task IDs returned in headers and can be set in general scripts
4. **Query Parameters**: General request-specific variables (businessKey, syncTimeout) as query parameters
5. **Centralized Documentation**: Complete documentation available through this app
6. **Standardized Validations**: General adjustments and verifications (e.g., validations)
7. **Camunda 8 Migration Ready**: Transparent switching of individual processes during migration
8. **Usage Tracking**: Automatic recording of which clients use which processes
9. **Unified Interface**: Replaces current mixed use of BPF and Camunda endpoints

This gateway serves as a unified layer between clients and Camunda, providing a consistent and well-documented interface while handling technical complexities internally.