@rem
@rem Licensed to the Apache Software Foundation (ASF) under one or more
@rem contributor license agreements.  See the NOTICE file distributed with
@rem this work for additional information regarding copyright ownership.
@rem The ASF licenses this file to You under the Apache License, Version 2.0
@rem (the "License"); you may not use this file except in compliance with
@rem the License.  You may obtain a copy of the License at
@rem
@rem     http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@echo off
if ""%1"" == ""-p"" goto doSetPort
if ""%1"" == """" goto doStart

echo Usage:  %0 [OPTIONS]
echo   -p [port]          Server port (default: 8899)
goto end

:doSetPort
shift
set PORT=%1

:doStart
set CFG_DIR=%~dp0%..
set CLASSPATH=%CFG_DIR%
set CLASSPATH=%~dp0..\lib\*;%~dp0..\ext-lib\*;%~dp0..\conf;%CLASSPATH%
set CONSOLE_MAIN=org.apache.shardingsphere.elasticjob.lite.console.ConsoleBootstrap
echo on
if ""%PORT%"" == """" set PORT=8899
java  -cp "%CLASSPATH%" %CONSOLE_MAIN% %PORT%

:end
