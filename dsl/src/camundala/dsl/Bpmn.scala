package camundala.dsl

case class Bpmn(id: Identifier,
                processes: Seq[BpmnProcess] = Seq.empty,
               ) {

  def processes(proc: BpmnProcess, procs: BpmnProcess*): Bpmn = copy(processes = (processes :+ proc) ++ procs)

  def ---(proc: BpmnProcess, procs: BpmnProcess*): Bpmn = processes(proc, procs: _*)

}

