package service;

import algoritmos.Floyd;
import algoritmos.MergeSort;
import algoritmos.resultado.ResultadoFloyd;
import datos.Plataforma;
import model.RedSocial;
import model.Usuario;

import java.util.Arrays;

/**
 * Consultas de difusión entre todos los pares de usuarios (Floyd).
 *
 * Floyd se corre una sola vez sobre la red y el resultado queda guardado: cada consulta se
 * responde desde las matrices, sin repetir el cálculo. Si la red cambió desde el último
 * cálculo (alta o baja de usuarios o vínculos), la próxima consulta vuelve a preprocesar.
 *
 * Javalin atiende cada pedido en su propio hilo y este service guarda estado, así que los
 * métodos públicos son synchronized.
 */
public class ConsultasDifusionService {

    private static final long SIN_CALCULAR = -1;

    private final Plataforma plataforma;

    private RedSocial redCalculada;
    private ResultadoFloyd<Usuario> resultado;
    private long versionCalculada = SIN_CALCULAR;

    public ConsultasDifusionService(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    /** Procesamiento inicial: corre Floyd sobre la red actual y guarda el resultado. */
    public synchronized void preprocesar() {
        // La versión se lee antes de copiar la red: si la red cambia en el medio, el resultado
        // queda con una versión vieja y se recalcula en la próxima consulta. Al revés podría
        // quedar guardada una red vieja con la versión nueva, y no se recalcularía nunca.
        long version = plataforma.getVersionRed();
        RedSocial red = plataforma.copiaDeRed();

        resultado = Floyd.calcular(red);
        redCalculada = red;
        versionCalculada = version;
    }

    /** True si ya se preprocesó y la red no cambió desde entonces. */
    public synchronized boolean estaActualizado() {
        return resultado != null && versionCalculada == plataforma.getVersionRed();
    }

    /** Un par sin cadena de difusión no es un error: el resultado lo indica con alcanzable = false. */
    public synchronized ConsultaDifusion consultar(int idOrigen, int idDestino) {
        asegurarActualizado();
        Usuario origen = buscarUsuario(idOrigen);
        Usuario destino = buscarUsuario(idDestino);

        boolean alcanzable = resultado.esAlcanzable(origen, destino);
        Integer tiempoTotal = alcanzable ? resultado.getDistancia(origen, destino) : null;

        return new ConsultaDifusion(origen, destino, alcanzable,
                resultado.getCamino(origen, destino), tiempoTotal);
    }

    /** Tiempos mínimos entre todos los pares, con los usuarios ordenados por id. */
    public synchronized TablaDifusion tablaDeTiempos() {
        asegurarActualizado();

        Usuario[] usuarios = redCalculada.getUsuarios().toArray(new Usuario[0]);
        MergeSort.mergeSort(usuarios, 0, usuarios.length - 1);

        Integer[][] tiempos = new Integer[usuarios.length][usuarios.length];
        for (int i = 0; i < usuarios.length; i++) {
            for (int j = 0; j < usuarios.length; j++) {
                if (resultado.esAlcanzable(usuarios[i], usuarios[j])) {
                    tiempos[i][j] = resultado.getDistancia(usuarios[i], usuarios[j]);
                }
            }
        }
        return new TablaDifusion(Arrays.asList(usuarios), tiempos);
    }

    private void asegurarActualizado() {
        if (!estaActualizado()) { preprocesar(); }
    }

    private Usuario buscarUsuario(int idUsuario) {
        Usuario usuario = redCalculada.getUsuario(idUsuario);
        if (usuario == null) { throw new IllegalArgumentException("No existe un usuario con id " + idUsuario); }
        return usuario;
    }
}
