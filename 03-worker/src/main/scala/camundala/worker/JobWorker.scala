package camundala.worker


import scala.concurrent.duration.*


trait JobWorker:
  def topic: String
  def timeout: Duration = 10.seconds



