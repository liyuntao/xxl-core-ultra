package com.xxl.job.core.biz.model;

public class JobResponseEntity {
	public static final int SUCCESS_CODE = 200;
	public static final int FAIL_CODE = 500;
	public static final int TIMEOUT_CODE = 502;

	public static final JobResponseEntity SUCCESS = new JobResponseEntity(SUCCESS_CODE, null);
	public static final JobResponseEntity FAIL = new JobResponseEntity(FAIL_CODE, null);
	public static final JobResponseEntity TIMEOUT = new JobResponseEntity(TIMEOUT_CODE, null);

	private final int code;
	private final String msg;

	public JobResponseEntity(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}

	@Override
	public String toString() {
		return "JobResponseEntity [code=" + code + ", msg=" + msg + "]";
	}

}
