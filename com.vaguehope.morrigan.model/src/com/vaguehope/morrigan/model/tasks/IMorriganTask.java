package com.vaguehope.morrigan.model.tasks;



public interface IMorriganTask {
	
	public String getTitle ();
	
	public TaskResult run (TaskEventListener taskEventListener);
	
}