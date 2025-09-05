package br.com.modware.transrv.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/painel", "/dashboard/ui"})
    public String dashboardPage() {
        return "forward:/dashboard.html";
    }
}


