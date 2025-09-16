package br.com.modware.transrv.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;

@Controller
public class PageController {

    @GetMapping({"/", "/painel", "/dashboard/ui"})
    public String dashboardPage() {
        return "forward:/dashboard.html";
    }

    @GetMapping(value = {"/notifications", "/notifications/ui", "/notificacoes", "/painel/notificacoes"}, produces = MediaType.TEXT_HTML_VALUE)
    public String notificationsPage() {
        return "forward:/notifications.html";
    }
}


