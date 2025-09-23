package br.com.modware.transrv.dto.dashboard;

public class DashboardConfigDTO {
    private long escalationLevel2Seconds;
    private long escalationLevel3Seconds;

    public long getEscalationLevel2Seconds() {
        return escalationLevel2Seconds;
    }

    public void setEscalationLevel2Seconds(long escalationLevel2Seconds) {
        this.escalationLevel2Seconds = escalationLevel2Seconds;
    }

    public long getEscalationLevel3Seconds() {
        return escalationLevel3Seconds;
    }

    public void setEscalationLevel3Seconds(long escalationLevel3Seconds) {
        this.escalationLevel3Seconds = escalationLevel3Seconds;
    }
}


