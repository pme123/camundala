package camundala.helper.util

import camundala.api.ApiConfig

trait Helpers:

  protected implicit lazy val workDir: os.Path =
    val wd = os.pwd
    println(s"Working Directory: $wd")
    wd
  end workDir

  protected def check(label: String, port: Int): Unit =
    var checking = true
    while checking do
      try
        os.proc(
          "curl",
          "--head",
          "--silent",
          "--output",
          "/dev/null",
          s"http://localhost:$port"
        ).callOnConsole()
        println(s"$label is ready to use!")
        println(s"Check http://localhost:$port")
        checking = false
      catch
        case _: Throwable =>
          println(s"waiting for $label ...")
          Thread.sleep(1000)
    end while
  end check

  extension (proc: os.proc)
    def callOnConsole(path: os.Path = os.pwd): os.CommandResult =
      proc.call(cwd = path, stdout = os.Inherit)
  end extension
end Helpers
