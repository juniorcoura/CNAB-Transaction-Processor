package br.com.juniorcoura.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.juniorcoura.backend.service.CnabService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/cnab")
public class CNABController {

    private final CnabService cnabService;

    public CNABController(CnabService cnabService) {
        this.cnabService = cnabService;
    }

    /*
     * A anotação @CrossOrigin (CORS) é utilizada para permitir que o frontend da aplicação,
     * rodando em um domínio diferente, possa fazer requisições
     * para este endpoint.
     */
    @PostMapping("/upload")
    @CrossOrigin(origins = {"http://localhost:9090"}) 
    public String upload(@RequestParam("file") MultipartFile file ) throws Exception {

        cnabService.uploadCnabFile(file);

        return "Processamento iniciado!";
    }
    
}
