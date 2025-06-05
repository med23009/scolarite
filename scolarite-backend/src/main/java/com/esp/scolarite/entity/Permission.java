package com.esp.scolarite.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


//Likely to be suppressed!

@RequiredArgsConstructor
public enum Permission {

    CHEF_POLE_CREATE("chef_pole:create"),
    CHEF_DEPT_CREATE("chef_dept:create"),
    CHEF_POLE_READ("chef_pole:read"),
    CHEF_DEPT_READ("chef_dept:read"),
    CHEF_POLE_UPDATE("chef_pole:update"),
    CHEF_DEPT_UPDATE("chef_dept:update"),
    CHEF_POLE_DELETE("chef_pole:delete"),
    CHEF_DEPT_DELETE("chef_dept:delete"),

    ADMIN_CREATE ("admin:create"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_READ("admin:read"),
    ADMIN_DELETE("admin:delete"),

    DE_CREATE("directeur_enseignement:create"),
    DE_READ("directeur_enseignement:read"),
    DE_UPDATE("directeur_enseignement:update"),
    DE_DELETE("directeur_enseignement:delete"),

    DSI_READ("directeur_systemes_informatiques:read"),
    DSI_CREATE("directeur_systemes_informatiques:read"),
    DSI_UPDATE("directeur_systemes_informatiques:read"),
    DSI_DELETE("directeur_systemes_informatiques:read"),

    RS_READ("responsable_scolarite:read"),
    ;

    @Getter
    private final String permission;


}
