package cl.recetaya.gateway.filter;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public JwtAuthGatewayFilterFactory(JwtAuthenticationFilter jwtAuthFilter) {
        super(Config.class);
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (jwtAuthFilter.esRutaPublica(request)) {
                return chain.filter(exchange);
            }

            String token = jwtAuthFilter.extraerToken(request);
            if (token == null || token.isBlank()) {
                return jwtAuthFilter.respuestaError(exchange, HttpStatus.UNAUTHORIZED, "Token de autorización requerido en cabecera Authorization Bearer");
            }

            try {
                Claims claims = jwtAuthFilter.validarToken(token);
                String username = claims.getSubject();
                String rol = claims.get("rol", String.class);

                // Propagar la identidad del usuario a los microservicios downstream
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-Auth-User", username != null ? username : "")
                        .header("X-Auth-Role", rol != null ? rol : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                return jwtAuthFilter.respuestaError(exchange, HttpStatus.UNAUTHORIZED, "Token JWT inválido o expirado");
            }
        };
    }

    public static class Config {
        // Propiedades de configuración en YAML si son requeridas
    }
}
