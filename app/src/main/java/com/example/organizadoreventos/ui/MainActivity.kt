package com.example.organizadoreventos.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.organizadoreventos.NotificacionReceiver
import com.example.organizadoreventos.R
import com.example.organizadoreventos.ui.fragmentos.ConsultarFragment
import com.example.organizadoreventos.ui.fragmentos.InicioFragment
import com.example.organizadoreventos.viewmodel.EventoViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import observeOnce

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var eventoViewModel: EventoViewModel
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNav: BottomNavigationView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        bottomNav = findViewById(R.id.bottom_nav)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        eventoViewModel = ViewModelProvider(this)[EventoViewModel::class.java]

        // Configuración de DrawerLayout
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Pantalla inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InicioFragment(), InicioFragment::class.java.simpleName)
                .commit()
        }
        crearCanalNotificacion()
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
                R.id.nav_respaldo -> {
                    eventoViewModel.todosLosEventos.observeOnce(this) { eventos ->
                        if (eventos.isNotEmpty()) {
                            DropboxBackupHelper.subirRespaldo(this, eventos)
                        } else {
                            Toast.makeText(this, "No hay eventos para respaldar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_restaurar -> {
                    DropboxBackupHelper.mostrarDialogoRespaldo(this, eventoViewModel)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
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

    private fun cargarFragment(fragment: androidx.fragment.app.Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        val fragmentTag = fragment::class.java.simpleName
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null || existingFragment.javaClass != fragment.javaClass) {
            fragmentTransaction.replace(R.id.fragment_container, fragment, fragmentTag)
            fragmentTransaction.commit()
        } else {
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Canal de Recordatorios"
            val descripcion = "Canal para notificaciones de eventos y recordatorios"
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel("canal_recordatorios", nombre, importancia)
            canal.description = descripcion

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)

            Log.d("MainActivity", "Canal de notificación creado o ya existe")
        }
    }

}
