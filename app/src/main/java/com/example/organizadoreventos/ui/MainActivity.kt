package com.example.organizadoreventos.ui

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.organizadoreventos.R
import com.example.organizadoreventos.ui.AcercaDeFragment
import com.example.organizadoreventos.ui.AnadirEventoFragment
import com.example.organizadoreventos.ui.fragmentos.ConsultarFragment
import com.example.organizadoreventos.ui.fragmentos.InicioFragment // Asegúrate de que esta ruta sea correcta
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        bottomNav = findViewById(R.id.bottom_nav)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configuración de DrawerLayout
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Pantalla inicial
        if (savedInstanceState == null) { // Evita recargar el fragmento al rotar la pantalla
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InicioFragment(), InicioFragment::class.java.simpleName)
                .commit()
        }


        // Menú de navegación lateral (Drawer)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_añadir_evento -> {
                    cargarFragment(AnadirEventoFragment())
                }
                R.id.nav_consultar_modificar -> {
                    cargarFragment(ConsultarFragment())
                }
                R.id.nav_acerca_de -> {
                    cargarFragment(AcercaDeFragment())
                }
                R.id.nav_salir_drawer -> {
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Navegación inferior (Bottom Navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    cargarFragment(InicioFragment())
                    true
                }
                R.id.nav_consultar -> {
                    cargarFragment(ConsultarFragment())
                    true
                }
                R.id.nav_salir -> {
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // Método común para cargar fragmentos y evitar recargar el mismo fragmento
    private fun cargarFragment(fragment: androidx.fragment.app.Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // Verificar si el fragmento ya está cargado
        val fragmentTag = fragment::class.java.simpleName
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null || existingFragment.javaClass != fragment.javaClass) {
            // Si el fragmento no existe o es de un tipo diferente al actual, reemplázalo
            fragmentTransaction.replace(R.id.fragment_container, fragment, fragmentTag)
            // Ya no se agrega a la pila de retroceso para evitar el "apilamiento"
            fragmentTransaction.commit()
        } else {
            // Si el fragmento ya está cargado y es del mismo tipo, no hagas nada
            // o simplemente muévelo al frente si usas show/hide
            // Para 'replace', si ya es el mismo, no necesitas hacer nada.
        }
    }

    // Manejo de la acción de retroceso del DrawerLayout
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Si no hay fragmentos en la pila (porque no usamos addToBackStack para los principales),
            // el super.onBackPressed() cerrará la actividad.
            // Si tuvieras otros fragmentos que SÍ agregas a la pila (ej. de detalles),
            // el super.onBackPressed() manejaría eso primero.
            super.onBackPressed()
        }
    }
}
