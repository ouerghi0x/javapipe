$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
java -jar "$scriptDir\javapipec.jar" @args

