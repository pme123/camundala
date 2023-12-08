package camundala.examples.demos.c7.service

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}

class PrintService extends JavaDelegate :

  @throws[Exception]
  def execute(execution: DelegateExecution): Unit =
    println("Called PrintService")