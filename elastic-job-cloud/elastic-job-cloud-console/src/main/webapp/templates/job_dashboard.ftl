<div class="content-wrapper">
    <section class="content-header">
        <h1>Dashboard</h1>
    </section>
    <hr style="background-color:#00c0ef;height:3px;width:100%;margin-bottom:0px;"> 
    <div class="box-body">
        <div class="row" >
            <div class="col-sm-6">
                <div class="box box-danger">
                    <div class="box-header with-border">
                        <h3 class="box-title">作业成功/失败图</h3>
                        <div class="box-tools pull-right">
                            <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                        </div>
                    </div>
                    <div class="box-body"> 
                        <div class="row">
                            <div class="col-sm-6" >
                                <div id="total_jobs_weekly" style="height:280px;"></div>
                            </div>
                            <div class="col-sm-6">
                                <div id="total_jobs_history" style="height:280px;"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="box box-danger">
                    <div class="box-header with-border">
                        <h3 class="box-title">作业分类</h3>
                        <div class="box-tools pull-right">
                            <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                        </div>
                    </div>
                    <div class="box-body">
                        <div class="row">
                            <div class="col-sm-6">
                                <div id="job_type" style="height:280px;"></div>
                            </div>
                            <div class="col-sm-6">
                                <div id="job_execution_type" style="height:280px;"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div> 
        </div>
        <div class="row">
            <div class="col-sm-12">
                <div class="box box-info">
                    <div class="box-header with-border">
                        <h3 class="box-title">作业/任务运行数</h3>
                        <div class="box-tools pull-right">
                            <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                        </div>
                    </div>
                    <div class="box-body">
                        <div class="row" style="height:250px;">
                            <div id="run_jobs" style="margin:0 auto;width:98%;height:250px;"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-12">
                <div class="box box-success">
                    <div class="box-header with-border">
                        <h3 class="box-title">接入平台作业数</h3>
                        <div class="box-tools pull-right">
                            <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                        </div>
                    </div>
                    <div class="box-body">
                        <div class="row">
                            <div id="import_jobs" style="margin:0 auto;width:98%;height:250px;"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
    
<script src="plugins/highcharts/js/highcharts.js"></script>
    
<script src="js/job_dashboard.js"></script>