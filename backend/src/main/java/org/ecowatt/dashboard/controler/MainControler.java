package org.ecowatt.dashboard.controler;

import org.ecowatt.dashboard.dto.rte.EcowattDto;
import org.ecowatt.dashboard.dto.web.DashboardDto;
import org.ecowatt.dashboard.service.EcowattService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class MainControler {

    private final EcowattService ecowattService;

    public MainControler(EcowattService ecowattService) {
        this.ecowattService = ecowattService;
    }

    @GetMapping("/main")
    private Mono<DashboardDto> getEmployeeById() {
        var res=ecowattService.getEcowatt();
        return res;
    }

}
