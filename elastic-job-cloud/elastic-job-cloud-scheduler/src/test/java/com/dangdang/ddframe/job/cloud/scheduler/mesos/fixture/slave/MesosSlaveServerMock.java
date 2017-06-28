package com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.slave;

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
public class MesosSlaveServerMock {
    
    @GET
    @Path("/state")
    public JsonObject state() throws JSONException {
        return (JsonObject) new JsonParser().parse("{\"version\":\"1.1.0\",\"build_date\":\"2017-02-27 10:51:31\",\"build_time\":1488163891.0,\"build_user\":\"gaohon"
                + "gtao\",\"start_time\":1488179767.60204,\"id\":\"d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"pid\":\"slave(1)@"
                + "127.0.0.1:9051\",\"hostname\":\"127.0.0.1\",\"resources\":{\"disk\":416050.0,\"mem\":6883.0,\"gpus\":0.0,\""
                + "cpus\":4.0,\"ports\":\"[31000-32000]\"},\"reserved_resources\":{},\"unreserved_resources\":{\"disk\":416050.0,\""
                + "mem\":6883.0,\"gpus\":0.0,\"cpus\":4.0,\"ports\":\"[31000-32000]\"},\"reserved_resources_full\":{},\"attributes\""
                + ":{},\"master_hostname\":\"127.0.0.1\",\"flags\":{\"appc_simple_discovery_uri_prefix\":\"http:\\/\\/\",\"appc_"
                + "store_dir\":\"\\/tmp\\/mesos\\/store\\/appc\",\"authenticate_http_readonly\":\"false\",\"authenticate_http_readw"
                + "rite\":\"false\",\"authenticatee\":\"crammd5\",\"authentication_backoff_factor\":\"1secs\",\"authorizer\":\"local\""
                + ",\"cgroups_cpu_enable_pids_and_tids_count\":\"false\",\"cgroups_enable_cfs\":\"false\",\"cgroups_hierarchy\":\""
                + "\\/sys\\/fs\\/cgroup\",\"cgroups_limit_swap\":\"false\",\"cgroups_root\":\"mesos\",\"container_disk_watch_interva"
                + "l\":\"15secs\",\"containerizers\":\"mesos\",\"default_role\":\"*\",\"disk_watch_interval\":\"1mins\",\"docker\":\"dock"
                + "er\",\"docker_kill_orphans\":\"true\",\"docker_registry\":\"https:\\/\\/registry-1.docker.io\",\"docker_remove_d"
                + "elay\":\"6hrs\",\"docker_socket\":\"\\/var\\/run\\/docker.sock\",\"docker_stop_timeout\":\"0ns\",\"docker_store_dir"
                + "\":\"\\/tmp\\/mesos\\/store\\/docker\",\"docker_volume_checkpoint_dir\":\"\\/var\\/run\\/mesos\\/isolators\\/docker"
                + "\\/volume\",\"enforce_container_disk_quota\":\"false\",\"executor_registration_timeout\":\"1mins\",\"executor_s"
                + "hutdown_grace_period\":\"5secs\",\"fetcher_cache_dir\":\"\\/tmp\\/mesos\\/fetch\",\"fetcher_cache_size\":\"2GB\",\""
                + "frameworks_home\":\"\",\"gc_delay\":\"1weeks\",\"gc_disk_headroom\":\"0.1\",\"hadoop_home\":\"\",\"help\":\"false\",\"ho"
                + "stname_lookup\":\"true\",\"http_authenticators\":\"basic\",\"http_command_executor\":\"false\",\"image_provision"
                + "er_backend\":\"copy\",\"initialize_driver_logging\":\"true\",\"ip\":\"127.0.0.1\",\"isolation\":\"cgroups\\/cpu"
                + ",cgroups\\/mem\",\"launcher\":\"linux\",\"launcher_dir\":\"\\/home\\/gaohongtao\\/mesos\\/mesos-1.1.0\\/build\\/src"
                + "\",\"logbufsecs\":\"0\",\"logging_level\":\"INFO\",\"master\":\"zk:\\/\\/localhost:4181,\\/mesos\",\"max_completed_ex"
                + "ecutors_per_framework\":\"150\",\"oversubscribed_resources_interval\":\"15secs\",\"perf_duration\":\"10secs\",\""
                + "perf_interval\":\"1mins\",\"port\":\"9051\",\"qos_correction_interval_min\":\"0ns\",\"quiet\":\"false\",\"recover\":\""
                + "reconnect\",\"recovery_timeout\":\"15mins\",\"registration_backoff_factor\":\"1secs\",\"revocable_cpu_low_prio"
                + "rity\":\"true\",\"runtime_dir\":\"\\/var\\/run\\/mesos\",\"sandbox_directory\":\"\\/mnt\\/mesos\\/sandbox\",\"strict\":"
                + "\"true\",\"switch_user\":\"true\",\"systemd_enable_support\":\"true\",\"systemd_runtime_directory\":\"\\/run\\/syst"
                + "emd\\/system\",\"version\":\"false\",\"work_dir\":\"\\/home\\/gaohongtao\\/mesos\\/work-1.1.0\"},\"frameworks\":[{\"i"
                + "d\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"name\":\"Elastic-Job-Cloud\",\"user\":\"gaohongtao\",\"failo"
                + "ver_timeout\":604800.0,\"checkpoint\":false,\"role\":\"*\",\"hostname\":\"127.0.0.1\",\"executors\":[{\"id\":\""
                + "foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"name\":\"\",\"source\":\"\",\"container\":\"53fb4af7-aee2-"
                + "44f6-9e47-6f418d9f27e1\",\"directory\":\"\\/home\\/gaohongtao\\/mesos\\/work-1.1.0\\/slaves\\/d8701508-41b7-47"
                + "1e-9b32-61cf824a660d-S0\\/frameworks\\/d8701508-41b7-471e-9b32-61cf824a660d-0000\\/executors\\/foo_app@-"
                + "@d8701508-41b7-471e-9b32-61cf824a660d-S0\\/runs\\/53fb4af7-aee2-44f6-9e47-6f418d9f27e1\",\"resources\":{\""
                + "disk\":0.0,\"mem\":512.0,\"gpus\":0.0,\"cpus\":2.5},\"tasks\":[{\"id\":\"cpu_job_1@-@1@-@READY@-@d8701508-41b7-4"
                + "71e-9b32-61cf824a660d-S0@-@ede114f0-f2db-4bad-b0e0-e820a7d19c59\",\"name\":\"cpu_job_1@-@1@-@READY@-@d87"
                + "01508-41b7-471e-9b32-61cf824a660d-S0\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"ex"
                + "ecutor_id\":\"foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"slave_id\":\"d8701508-41b7-471e-9b32-6"
                + "1cf824a660d-S0\",\"state\":\"TASK_RUNNING\",\"resources\":{\"disk\":0.0,\"mem\":128.0,\"gpus\":0.0,\"cpus\":0.5},\"s"
                + "tatuses\":[{\"state\":\"TASK_RUNNING\",\"timestamp\":1488186955.00194,\"container_status\":{\"network_infos\":["
                + "{\"ip_addresses\":[{\"ip_address\":\"127.0.0.1\"}]}]}}]},{\"id\":\"cpu_job_1@-@0@-@READY@-@d8701508-41b7-"
                + "471e-9b32-61cf824a660d-S0@-@31a2862f-c68c-448d-bbcf-8815b5c2dfef\",\"name\":\"cpu_job_1@-@0@-@READY@-@d8"
                + "701508-41b7-471e-9b32-61cf824a660d-S0\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\"e"
                + "xecutor_id\":\"foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"slave_id\":\"d8701508-41b7-471e-9b32-"
                + "61cf824a660d-S0\",\"state\":\"TASK_RUNNING\",\"resources\":{\"disk\":0.0,\"mem\":128.0,\"gpus\":0.0,\"cpus\":0.5},\""
                + "statuses\":[{\"state\":\"TASK_RUNNING\",\"timestamp\":1488186955.00214,\"container_status\":{\"network_infos\":"
                + "[{\"ip_addresses\":[{\"ip_address\":\"127.0.0.1\"}]}]}}]},{\"id\":\"cpu_job_1@-@2@-@READY@-@d8701508-41b7"
                + "-471e-9b32-61cf824a660d-S0@-@a03017a7-f520-483b-afce-2a5685d0ca2e\",\"name\":\"cpu_job_1@-@2@-@READY@-@d"
                + "8701508-41b7-471e-9b32-61cf824a660d-S0\",\"framework_id\":\"d8701508-41b7-471e-9b32-61cf824a660d-0000\",\""
                + "executor_id\":\"foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0\",\"slave_id\":\"d8701508-41b7-471e-9b32"
                + "-61cf824a660d-S0\",\"state\":\"TASK_RUNNING\",\"resources\":{\"disk\":0.0,\"mem\":128.0,\"gpus\":0.0,\"cpus\":0.5},"
                + "\"statuses\":[{\"state\":\"TASK_RUNNING\",\"timestamp\":1488186955.00174,\"container_status\":{\"network_infos\""
                + ":[{\"ip_addresses\":[{\"ip_address\":\"127.0.0.1\"}]}]}}]}],\"queued_tasks\":[],\"completed_tasks\":[]}],\""
                + "completed_executors\":[]}],\"completed_frameworks\":[]}");
    }
}
