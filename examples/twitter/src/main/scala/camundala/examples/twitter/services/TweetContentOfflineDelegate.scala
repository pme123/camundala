package camundala
package examples.twitter
package services

@Service(services.tweetAdapter)
class TweetContentOfflineDelegate extends JavaDelegate :

  @throws[Exception]
  override def execute(execution: DelegateExecution): Unit =
    val content = execution.getVariable("content").asInstanceOf[String]
    System.out.println("\n\n\n######\n\n\n")
    System.out.println("NOW WE WOULD TWEET: '" + content + "'")
    System.out.println("\n\n\n######\n\n\n")

