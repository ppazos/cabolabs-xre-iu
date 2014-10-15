class BootStrap {

    def init = { servletContext ->
       
       // TODO: configurable
       TimeZone.'default' = TimeZone.getTimeZone('America/Montevideo') // Con este considera daylight savings (cambios de +/- una hora por anio)
    }
    def destroy = {
    }
}
