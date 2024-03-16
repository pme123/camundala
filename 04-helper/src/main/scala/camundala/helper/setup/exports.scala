package camundala.helper.setup

val doNotAdjust = "DO NOT ADJUST"

def createOrUpdate(file: os.Path, contentNew: String): Unit =
  val contentExisting =
    if os.exists(file)
    then os.read(file)
    else doNotAdjust
  val contentUpdated =
    if contentExisting.contains(doNotAdjust) 
    then contentNew
    else contentExisting
  os.write.over(file, contentUpdated)
  
end createOrUpdate

