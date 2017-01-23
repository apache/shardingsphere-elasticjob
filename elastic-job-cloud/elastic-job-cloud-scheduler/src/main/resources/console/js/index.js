$(document).ready(function() {
    $('#contentRight').load('job_overview.html');
    $('#register').click(function() {
        $('#contentRight').load('job_overview.html');
    });
    $('#status').click(function() {
        $('#contentRight').load('job_status.html');
    });
    $('#exec_detail').click(function() {
        $('#contentRight').load('job_exec_detail.html');
    });
    $('#exec_status').click(function() {
        $('#contentRight').load('job_exec_status.html');
    });
    $('#dashboard').click(function() {
        $('#contentRight').load('job_dashboard.html');
    });
});
