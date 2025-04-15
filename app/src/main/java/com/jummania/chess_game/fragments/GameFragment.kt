package com.jummania.chess_game.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jummania.ChessView
import com.jummania.SymbolStyle
import com.jummania.chess_game.R


/**
 * Created by Jummania on 13/4/25.
 * Email: sharifuddinjumman@gmail.com
 * Dhaka, Bangladesh.
 */
class GameFragment : Fragment(R.layout.fragment_game) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mActivity = activity ?: return

        val chessView = view.findViewById<ChessView>(R.id.chessView)

        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(mActivity)

        preferenceManager?.apply {

            val playMusic = getBoolean("playMusic", false)
            if (playMusic) {
                Toast.makeText(context, "Play music is On", Toast.LENGTH_SHORT).show()
            }

            chessView.setSoundEffectEnabled(getBoolean("clickSound", true))

            chessView.setBackgroundColor(getInt("backgroundColor", Color.WHITE))

            chessView.setPieceStyle(
                getBoolean("isLightFilled", true),
                getBoolean("isDarkFilled", true),
                getInt("pieceLightColor", Color.WHITE),
                getInt("pieceDarkColor", Color.BLACK)
            )

            chessView.setSquareColors(
                getInt("lightSquareColor", "#fadeaf".toColorInt()),
                getInt("darkSquareColor", "#8e4f19".toColorInt())
            )

            chessView.setEnableStroke(
                getBoolean("enableStroke", true),
                getInt("strokeLightColor", Color.WHITE),
                getInt("strokeDarkColor", Color.BLACK)
            )

            chessView.setSymbolStyle(
                SymbolStyle.fromInt(
                    getString("symbolStyle", "1")!!.toInt()
                ), getBoolean("isBoldSymbol", false)
            )
        }

        val mWindow = mActivity.window
        WindowInsetsControllerCompat(
            mWindow, mActivity.findViewById(android.R.id.content)
        ).let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            MaterialAlertDialogBuilder(mActivity).setTitle("Are you sure you want to exit?")
                .setMessage("Any unsaved progress may be lost.")
                .setPositiveButton("Exit") { _, _ -> mActivity.finish() }
                .setNegativeButton("Cancel", null).show()
        }
    }


}