package org.tyaa.demo.java.springboot.brokershop.application.methods.controllers;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.tyaa.demo.java.springboot.brokershop.controller.AuthController;
import org.tyaa.demo.java.springboot.brokershop.models.RoleModel;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Класс тестов приложения для проверки рест-контроллера безопасности
 * прямыми вызовами его методов действий на серверной стороне
 * с полноценным доступом только к слою контроллеров
 * (другие составляющие приложения при необходимости нужно получать
 * в виде макетов, например, используя внедрение макетов аннотациями MockBean)
 * */

// включение режима теста приложения без запуска на реальном веб-сервере,
// без доступа к контексту приложения
// и к реализациям составляющих приложения вне слоя контроллеров
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerMethodsTest {

    // Обычное внедрение зависимости,
    // потому что в тестах приложения доступен функционал Spring
    @Autowired
    public AuthController authController;

    @Test
    @Order(1)
    public void givenGuestUserWhenGetAllRolesThenThrowsAuthenticationCredentialsNotFoundException () {
        // проверить, что вызов метода getAllRoles из объекта контроллера
        // выбросит исключение, потому что выполняется
        // неаутентифицированным пользователем
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            authController.getRoles();
        });
    }

    // проверка возможности получения стандартной модели данных о пользователе
    // по его имени из пользовательского бина hibernateWebAuthProvider,
    // из стандартного метода loadUserByUsername
    @Test
    @WithUserDetails(
        value = "admin",
        userDetailsServiceBeanName = "hibernateWebAuthProvider")
    @Order(2)
    public void givenAdminUserWhenLoadUserByUsernameThenReturnUserDetails () {}

    // успешный вызов защищенного метода пользователем
    // с именем admin и ролью ADMIN
    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    @Order(3)
    public void givenAdminUserWithAdminRoleWhenGetRolesThenReturnResponseEntityWithOkStatus() {
        ResponseEntity responseEntity = authController.getRoles();
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    // успешный вызов защищенного метода пользователем
    // только с явно указанной ролью ADMIN
    @Test
    @WithMockUser(roles = { "ADMIN" })
    @Order(4)
    public void givenUserWithAdminRoleWhenGetRolesThenReturnResponseEntityWithOkStatus () {
        ResponseEntity responseEntity = authController.getRoles();
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    @Order(5)
    public void givenRoleWithAdminRoleWhenCreateRoleThenReturnResponseEntityWithCreatedStatus(){
        final RoleModel roleModel =
                RoleModel.builder()
                        .name("ROLE_DEMO")
                        .build();
        ResponseEntity responseEntity = authController.createRole(roleModel);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.CREATED);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    @Order(5)
    public void givenRoleWithUserRoleWhenCreateRoleThenReturnResponseEntityWithBadGetawayStatus(){
        final RoleModel roleModel =
                RoleModel.builder()
                        .name("ROLE_DEMO")
                        .build();
        ResponseEntity responseEntity = authController.createRole(roleModel);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_GATEWAY);
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    @Order(5)
    public void givenUserWithAdminRoleWhenMakeUserAdminThenReturnResponseEntityWithOkStatus() throws Exception {
        ResponseEntity responseEntity = authController.makeUserAdmin(1L);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    @Order(5)
    public void givenUserWithUserRoleWhenMakeUserAdminThenReturnResponseEntityWithNotFoundStatus() throws Exception {
        ResponseEntity responseEntity = authController.makeUserAdmin(1L);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }
}
