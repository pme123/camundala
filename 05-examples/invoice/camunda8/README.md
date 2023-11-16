# The Camunda Invoice example in a Spring Boot Web Application
> This is the invoice demo application which is shipped with the full distributions in Camunda 7.

See the original [README](https://github.com/camunda/camunda-bpm-platform/tree/master/examples/invoice).

I bundled it here! Just run `InvoiceProcessApplication`.

> This requires that **Camunda 8 Standalone** is running -> _https://github.com/camunda/camunda-platform.git_

This based on this Project: https://github.com/pme123/spring-boot-datakurre-plugins

## Migration BPMN/DMN to Camunda 8
### Pain Points
- The Migration Plugin did not work for DMNs - I had to adjust the XML manually.
- Forms can not be linked (copy-paste Json of form) (Standalone version).
- Camunda Property Infos Plugin does not work.
- FEEL is missing _**not** myProperty_ - you need to do it _myProperty = false_.
- CallActivities Input-Mapping were not migrated.

### What did not work
- Forms in the Start Event
  - Workaround: Added another UserTask 
  - but with this, it is impossible to have Camunda Processes running by itself (no other application must be deployed)
- No files
  - Workaround: only the reference to the file is in the process.
- Dynamic content in Form Texts (FEEL)
- Datastore / Data object reference have no graphic anymore

### Cool stuff
- Some code completion in FEEL expressions
- Links in the Modeller to the documentation

## Migrating Camundala to Camunda 8
The work to migrate the Example concerning:
- API Documentation
- Test Simulation
- Validation

### API Documentation
Nothing to do here - the API stays the same! 🎉🎉🎉
### Test Simulation

