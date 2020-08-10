package camundala.dsl

case class UserTask(id: Identifier,
                    candidateGroups: CandidateGroups = CandidateGroups.none,
                    candidateUsers: CandidateUsers = CandidateUsers.none,
                    maybeForm: Option[BpmnForm] = None,
                    properties: Properties = Properties.none,
                    inputParams: Parameters = Parameters.none,
                    outputParams: Parameters = Parameters.none,
                    inFlows: Seq[SequenceFlow] = Seq.empty,
                    outFlows: Seq[SequenceFlow] = Seq.empty
                   ) extends IdentifiableNode {

  def canEdit(group: BpmnGroup, groups: BpmnGroup*): UserTask = copy(candidateGroups = (candidateGroups :+ group) ++ groups)

  def canEdit(user: BpmnUser, users: BpmnUser*): UserTask = copy(candidateUsers = (candidateUsers :+ user) ++ users)

}
