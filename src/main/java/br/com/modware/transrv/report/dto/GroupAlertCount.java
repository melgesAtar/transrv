package br.com.modware.transrv.report.dto;

import java.util.List;

public record GroupAlertCount(String groupName, List<AlertCount> alerts) {
}
