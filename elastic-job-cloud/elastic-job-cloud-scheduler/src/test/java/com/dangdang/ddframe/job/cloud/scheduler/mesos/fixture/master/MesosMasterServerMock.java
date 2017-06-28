package com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.master;

/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class MesosMasterServerMock {
    
    @GET
    @Path("/state")
    public JsonObject state() throws JSONException {
        return (JsonObject) new JsonParser().parse("{\"version\":\"1.1.0\",\"build_date\":\"2017-02-27 10:51:31\",\"build_time\":1488163891.0,\"build_user\":\"gaohongtao\",\"start_time\""
                + ":1488179758.62289,\"elected_time\":1488179758.69795,\"id\":\"d8701508-41b7-471e-9b32-61cf824a660d\",\"pid\":\"master@127.0.0.1:9050\",\"hostname\":\"127.0.0.1\","
                + "\"activated_slaves\":1.0,\"deactivated_slaves\":0.0,\"leader\":\"master@127.0.0.1:9050\",\"leader_info\":{\"id\":\"d8701508-41b7-471e-9b32-61cf824a660d\","
                + "\"pid\":\"master@127.0.0.1:9050\",\"port\":9050,\"hostname\":\"127.0.0.1\"},\"flags\":{\"agent_ping_timeout\":\"15secs\",\"agent_reregister_timeout\":\"10mins\","
                + "\"allocation_interval\":\"1secs\",\"allocator\":\"HierarchicalDRF\",\"authenticate_agents\":\"false\",\"authenticate_frameworks\":\"false\","
                + "\"authenticate_http_frameworks\":\"false\",\"authenticate_http_readonly\":\"false\",\"authenticate_http_readwrite\":\"false\",\"authenticators\":\"crammd5\","
                + "\"authorizers\":\"local\",\"framework_sorter\":\"drf\",\"help\":\"false\",\"hostname_lookup\":\"true\",\"http_authenticators\":\"basic\","
                + "\"initialize_driver_logging\":\"true\",\"ip\":\"127.0.0.1\",\"log_auto_initialize\":\"true\",\"logbufsecs\":\"0\",\"logging_level\":\"INFO\",\"max_agent_ping_timeouts\":\"5\","
                + "\"max_completed_frameworks\":\"50\",\"max_completed_tasks_per_framework\":\"1000\",\"port\":\"9050\",\"quiet\":\"false\",\"quorum\":\"1\",\"recovery_agent_removal_limit\":\"100%\","
                + "\"registry\":\"replicated_log\",\"registry_fetch_timeout\":\"1mins\",\"registry_gc_interval\":\"15mins\",\"registry_max_agent_age\":\"2weeks\",\"registry_max_agent_count\""
                + ":\"102400\",\"registry_store_timeout\":\"20secs\",\"registry_strict\":\"false\",\"root_submissions\":\"true\",\"user_sorter\":\"drf\",\"version\":\"false\",\"webui_dir\":\"\\/home"
                + "\\/gaohongtao\\/mesos\\/mesos-1.1.0\\/build\\/..\\/src\\/webui\",\"work_dir\":\"\\/home\\/gaohongtao\\/mesos\\/work-1.1.0\",\"zk\":\"zk:\\/\\/localhost:4181,\\/mesos\","
                + "\"zk_session_timeout\":\"10secs\"},\"slaves\":[{\"id\":\"d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"pid\":\"slave(1)@127.0.0.1:9051\",\"hostname\":\"127.0.0.1\","
                + "\"registered_time\":1488179768.08728,\"resources\":{\"disk\":416050.0,\"mem\":6883.0,\"gpus\":0.0,\"cpus\":4.0,\"ports\":\"[31000-32000]\"},\"used_resources\":"
                + "{\"disk\":0.0,\"mem\":512.0,\"gpus\":0.0,\"cpus\":2.5},\"offered_resources\":{\"disk\":416050.0,\"mem\":6371.0,\"gpus\":0.0,\"cpus\":1.5,\"ports\":\"[31000-32000]\"},"
                + "\"reserved_resources\":{},\"unreserved_resources\":{\"disk\":416050.0,\"mem\":6883.0,\"gpus\":0.0,\"cpus\":4.0,\"ports\":\"[31000-32000]\"},\"attributes\":{},\"active\":true,"
                + "\"version\":\"1.1.0\"}],\"frameworks\":[{\"id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"name\":\"Elastic-Job-Cloud\",\"pid\":"
                + "\"scheduler-da326b36-34ed-4b6e-ac40-0c936f76be4e@127.0.0.1:53639\",\"used_resources\":{\"disk\":0.0,\"mem\":512.0,\"gpus\":0.0,\"cpus\":2.5},\"offered_resources\""
                + ":{\"disk\":416050.0,\"mem\":6371.0,\"gpus\":0.0,\"cpus\":1.5,\"ports\":\"[31000-32000]\"},\"capabilities\":[],\"hostname\":\"127.0.0.1\",\"webui_url\":\"http:\\/"
                + "\\/127.0.0.1:8899\",\"active\":true,\"user\":\"gaohongtao\",\"failover_timeout\":604800.0,\"checkpoint\":false,\"role\":\"*\",\"registered_time\":1488179830.94584,"
                + "\"unregistered_time\":0.0,\"resources\":{\"disk\":416050.0,\"mem\":6883.0,\"gpus\":0.0,\"cpus\":4.0,\"ports\":\"[31000-32000]\"},\"tasks\":[{\"id\":"
                + "\"cpu_job_1@-@2@-@READY@-@d8701508-41b7-471e-9b32-61cf824a660d-S0@-@a03017a7-f520-483b-afce-2a5685d0ca2e\",\"name\":\"cpu_job_1@-@2@-@READY@-@d8701508-41b7-471e-"
                + "9b32-61cf824a660d-S0\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"executor_id\":\"foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"slave_id\":"
                + "\"d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"state\":\"TASK_RUNNING\",\"resources\":{\"disk\":0.0,\"mem\":128.0,\"gpus\":0.0,\"cpus\":0.5},\"statuses\":[{\"state\""
                + ":\"TASK_RUNNING\",\"timestamp\":1488179870.00284,\"container_status\":{\"network_infos\":[{\"ip_addresses\":[{\"ip_address\":\"127.0.0.1\"}]}]}}]},{\"id\":"
                + "\"cpu_job_1@-@0@-@READY@-@d8701508-41b7-471e-9b32-61cf824a660d-S0@-@31a2862f-c68c-448d-bbcf-8815b5c2dfef\",\"name\":\"cpu_job_1@-@0@-@READY@-@d8701508-41b7-471e-"
                + "9b32-61cf824a660d-S0\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"executor_id\":\"foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"slave_id\":"
                + "\"d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"state\":\"TASK_RUNNING\",\"resources\":{\"disk\":0.0,\"mem\":128.0,\"gpus\":0.0,\"cpus\":0.5},\"statuses\":[{\"state\":"
                + "\"TASK_RUNNING\",\"timestamp\":1488179870.00305,\"container_status\":{\"network_infos\":[{\"ip_addresses\":[{\"ip_address\":\"127.0.0.1\"}]}]}}]},{\"id\":"
                + "\"cpu_job_1@-@1@-@READY@-@d8701508-41b7-471e-9b32-61cf824a660d-S0@-@ede114f0-f2db-4bad-b0e0-e820a7d19c59\",\"name\":\"cpu_job_1@-@1@-@READY@-@d8701508-41b7-471e-"
                + "9b32-61cf824a660d-S0\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"executor_id\":\"foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"slave_id\":"
                + "\"d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"state\":\"TASK_RUNNING\",\"resources\":{\"disk\":0.0,\"mem\":128.0,\"gpus\":0.0,\"cpus\":0.5},\"statuses\":"
                + "[{\"state\":\"TASK_RUNNING\",\"timestamp\":1488179870.00294,\"container_status\":{\"network_infos\":[{\"ip_addresses\":[{\"ip_address\":\"127.0.0.1\"}]}]}}]}],"
                + "\"completed_tasks\":[],\"offers\":[{\"id\":\"d8701508-41b7-471e-9b32-61cf824a660d-O1\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"slave_id\":"
                + "\"d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"resources\":{\"disk\":416050.0,\"mem\":6371.0,\"gpus\":0.0,\"cpus\":1.5,\"ports\":\"[31000-32000]\"}}],\"executors\":"
                + "[{\"executor_id\":\"foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"name\":\"\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"command\":"
                + "{\"shell\":true,\"value\":\"bin\\/start.sh\",\"argv\":[],\"uris\":[{\"value\":\"http:\\/\\/127.0.0.1\\/image\\/es-test-1.0.tar.gz\",\"executable\":false}]},"
                + "\"resources\":{\"disk\":0.0,\"mem\":128.0,\"gpus\":0.0,\"cpus\":1.0},\"slave_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-S0\"}]}],\"completed_frameworks\":[],"
                + "\"orphan_tasks\":[],\"unregistered_frameworks\":[]}");
    }
}
