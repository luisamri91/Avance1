package com.fidecompro.cliente;

import com.fidecompro.modelo.Producto;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * Pide la lista de productos al servidor en un hilo aparte (SwingWorker), para que la
 * ventana no se congele mientras llega la respuesta. Al terminar, entrega la lista en el hilo de Swing.
 */
public class HiloActualizacionStock extends SwingWorker<List<Producto>, Void> {

    private final Sesion sesion;
    private final Consumer<List<Producto>> alTerminar;

    public HiloActualizacionStock(Sesion sesion, Consumer<List<Producto>> alTerminar) {
        this.sesion = sesion;
        this.alTerminar = alTerminar;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Producto> doInBackground() {
        Respuesta r = sesion.enviar(TipoOperacion.LISTAR_PRODUCTOS, null);
        return r != null && r.isExito() ? (List<Producto>) r.getDatos() : null;
    }

    @Override
    protected void done() {
        try {
            List<Producto> lista = get();
            if (lista != null) {
                alTerminar.accept(lista);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("No se pudo actualizar la lista de productos: " + e.getMessage());
        }
    }
}
