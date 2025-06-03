package com.example.organizadoreventos

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        bottomNav = findViewById(R.id.bottom_nav)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Pantalla inicial
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, InicioFragment())
            .commit()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_añadir_evento -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AnadirEventoFragment())
                        .commit()
                }
                R.id.nav_consultar_modificar -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ConsultarFragment())
                        .commit()
                }
                R.id.nav_respaldo -> {
                    // Temporalmente mostramos ConsultarFragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ConsultarFragment())
                        .commit()
                }
                R.id.nav_restaurar -> {
                    // Temporalmente mostramos ConsultarFragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ConsultarFragment())
                        .commit()
                }
                R.id.nav_acerca_de -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AcercaDeFragment())
                        .commit()
                }
                R.id.nav_salir_drawer -> {
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, InicioFragment())
                        .commit()
                    true
                }
                R.id.nav_consultar -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ConsultarFragment())
                        .commit()
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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }
}
