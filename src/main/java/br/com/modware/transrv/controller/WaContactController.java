package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.wacontact.WAContactCreateRequest;
import br.com.modware.transrv.dto.wacontact.WAContactPageResponse;
import br.com.modware.transrv.dto.wacontact.WAContactResponse;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.service.WAContactQueryService;
import br.com.modware.transrv.service.WAContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/wa-contacts")
@Tag(name = "Contatos WhatsApp", description = "Endpoints para consulta e gerenciamento de contatos de WhatsApp")
@SecurityRequirement(name = "JWT")
public class WaContactController {
    
    private final WAContactQueryService waContactQueryService;
    private final WAContactService waContactService;

    public WaContactController(WAContactQueryService waContactQueryService, WAContactService waContactService) {
        this.waContactQueryService = waContactQueryService;
        this.waContactService = waContactService;
    }

    @Operation(
        summary = "Listar contatos WhatsApp",
        description = "Retorna uma lista paginada de contatos WhatsApp"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contatos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = WAContactPageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<WAContactPageResponse> getWAContacts(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo para ordenação (id, name, phoneNumber)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        WAContactPageResponse response = waContactQueryService.findAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Criar novo contato WhatsApp",
        description = "Cria um novo contato WhatsApp. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contato criado com sucesso",
            content = @Content(schema = @Schema(implementation = WAContactResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (nome vazio, telefone vazio ou já existe)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<WAContactResponse> createWAContact(
            @Parameter(description = "Dados do novo contato WhatsApp")
            @RequestBody WAContactCreateRequest request) {
        
        WAContact waContact = waContactService.create(request.name(), request.phoneNumber());
        WAContactResponse response = WAContactResponse.from(waContact);
        return ResponseEntity.ok(response);
    }
}