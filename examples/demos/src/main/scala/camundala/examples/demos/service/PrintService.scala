package camundala.examples.demos.service

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}

class PrintService extends JavaDelegate :

  @throws[Exception]
  def execute(execution: DelegateExecution): Unit =
    println("Called PrintService")