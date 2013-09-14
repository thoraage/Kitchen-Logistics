resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"

//libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v + "-0.2.11"))
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")

//addSbtPlugin("net.databinder" % "giter8-plugin" % "0.3.2")
