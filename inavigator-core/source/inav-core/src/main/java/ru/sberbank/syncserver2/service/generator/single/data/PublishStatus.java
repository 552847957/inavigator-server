package ru.sberbank.syncserver2.service.generator.single.data;

public enum PublishStatus {

	PUBLISHED_ACTUAL("Опубликовано/актуально",false),
	PUBLISHED_NOT_ACTUAL("Опубликовано/не актуально",true),
	DRAFT_ONLY("Черновик",true),
	UNKNOWN("Не определено",false);

	private String statusName;
	private boolean canPublish;

	
	
	private PublishStatus(String statusName, boolean canPublish) {
		this.statusName = statusName;
		this.canPublish = canPublish;
	}

	public boolean isCanPublish() {
		return canPublish;
	}

	public void setCanPublish(boolean canPublish) {
		this.canPublish = canPublish;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	
	
}
