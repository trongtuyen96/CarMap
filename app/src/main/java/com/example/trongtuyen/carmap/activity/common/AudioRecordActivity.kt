package com.example.trongtuyen.carmap.activity.common

import android.Manifest
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.trongtuyen.carmap.R
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import java.io.IOException
import android.support.v4.app.ActivityCompat
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.trongtuyen.carmap.controllers.AppController
import com.example.trongtuyen.carmap.utils.AudioPlayer
import com.sdsmdg.tastytoast.TastyToast
import java.util.*
import java.util.concurrent.TimeUnit


class AudioRecordActivity : AppCompatActivity() {

    @BindView(R.id.layoutRecord_audio_record)
    lateinit var btnRecord: LinearLayout
    @BindView(R.id.layoutPlay_audio_record)
    lateinit var btnPlay: LinearLayout
    @BindView(R.id.imPlay_audio_record)
    lateinit var imPlay: ImageView
    @BindView(R.id.imRecord_audio_record)
    lateinit var imRecord: ImageView
    @BindView(R.id.tvRecord_audio_record)
    lateinit var tvRecord: TextView
    @BindView(R.id.tvPlay_audio_record)
    lateinit var tvPlay: TextView
    @BindView(R.id.btnClose_audio_record)
    lateinit var btnCLose: ImageView

    @BindView(R.id.txtProgress_audio_record)
    lateinit var tvProgress: TextView
    @BindView(R.id.progressBar_audio_record)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.btnSend_audio_record)
    lateinit var btnSend: Button
    @BindView(R.id.btnDismiss_audio_record)
    lateinit var btnDismiss: Button

    @BindView(R.id.tvPath_audio_record)
    lateinit var tvPath: TextView


    private val LOG_TAG = "AudioRecordTest"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var mFileName: String = ""

    //    private var mRecordButton: RecordButton? = null
    private var mRecorder: MediaRecorder? = null

    //    private var mPlayButton: PlayButton? = null
    private var mPlayer: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf<String>(Manifest.permission.RECORD_AUDIO)

    // Kiểm tra đã record
    private var bRecorded: Boolean = false

    // Audio Player
    private var mAudioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        ButterKnife.bind(this)
        initComponents()

        // Record to the external cache directory for visibility
        mFileName = externalCacheDir!!.absolutePath
        mFileName += "/audio_new.3gp"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

//        val ll = LinearLayout(this)
//        mRecordButton = RecordButton(this)
//        ll.addView(mRecordButton,
//                LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0f))
//        mPlayButton = PlayButton(this)
//        ll.addView(mPlayButton,
//                LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0f))
//        setContentView(ll)

    }

    private fun initComponents() {
        var mStartRecording = true
        var mStartPlaying = true
        lateinit var countDownTimer: CountDownTimer
        btnRecord.setOnClickListener {
            onRecord(mStartRecording)
            if (mStartRecording) {
                tvRecord.text = "DỪNG GHI"
                btnRecord.background = getDrawable(R.drawable.bg_btn_dismiss)
                imRecord.setImageResource(R.drawable.ic_stop_white_48dp)

                /////////////////////////
                countDownTimer = object : CountDownTimer(7000, 100) {
                    override fun onTick(millisUntilFinished: Long) {
                        tvProgress.text = String.format(Locale.getDefault(), "%s%d",
                                "0",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1)
                        progressBar.progress = millisUntilFinished.times(100).div(7000).toInt()
                    }

                    override fun onFinish() {
                        progressBar.progress = 0
                        tvProgress.text = "00"
                        btnRecord.performClick()
                    }
                }.start()
            } else {
                countDownTimer.cancel()
                tvRecord.text = "BẮT ĐẦU"
                btnRecord.background = getDrawable(R.drawable.bg_btn_send)
                imRecord.setImageResource(R.drawable.ic_record_voice_over_white_48dp)
                TastyToast.makeText(this, "Đã ghi âm thành công!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show()
                tvPath.text = mFileName
                bRecorded = true

                // Chạy audio
                if (AppController.soundMode == 1) {
                    when (AppController.voiceType) {
                        1 -> {
                            mAudioPlayer.play(this, R.raw.ghi_am_thanh_cong)
                        }
                        2 -> {
                            mAudioPlayer.play(this, R.raw.ghi_am_thanh_cong_2)
                        }
                    }
                }
            }
            mStartRecording = !mStartRecording
        }
        btnPlay.setOnClickListener {
            onPlay(mStartPlaying)
            if (mStartPlaying) {
                tvPlay.text = "DỪNG NGHE"
                btnPlay.background = getDrawable(R.drawable.bg_btn_orange)
                imPlay.setImageResource(R.drawable.ic_pause_white_48dp)
            } else {
                tvPlay.text = "NGHE"
                btnPlay.background = getDrawable(R.drawable.bg_btn_green)
                imPlay.setImageResource(R.drawable.ic_play_arrow_white_48dp)
            }
            mStartPlaying = !mStartPlaying
        }
        btnCLose.setOnClickListener {
            finish()
        }
        btnSend.setOnClickListener {
            if (bRecorded) {
//                AppController.fileAudioName = mFileName!!
                intent.putExtra("FileAudioPath", mFileName)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                TastyToast.makeText(this, "Bạn cần ghi âm trước khi chọn", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show()
            }
        }
        btnDismiss.setOnClickListener {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()

    }

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying() {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setDataSource(mFileName)
            mPlayer!!.prepare()
            mPlayer!!.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

    }

    private fun stopPlaying() {
        mPlayer!!.release()
        mPlayer = null
    }

    private fun startRecording() {
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder!!.setOutputFile(mFileName)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mRecorder!!.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

        mRecorder!!.start()
    }

    private fun stopRecording() {
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
    }

//    internal inner class RecordButton(ctx: Context) : Button(ctx) {
//        var mStartRecording = true
//
//        var clicker: OnClickListener = object : OnClickListener {
//            override fun onClick(v: View) {
//                onRecord(mStartRecording)
//                if (mStartRecording) {
//                    setText("Stop recording")
//                } else {
//                    setText("Start recording")
//                }
//                mStartRecording = !mStartRecording
//            }
//        }
//
//        init {
//            setText("Start recording")
//            setOnClickListener(clicker)
//        }
//    }
//
//    internal inner class PlayButton(ctx: Context) : Button(ctx) {
//        var mStartPlaying = true
//
//        var clicker: OnClickListener = object : OnClickListener {
//            override fun onClick(v: View) {
//                onPlay(mStartPlaying)
//                if (mStartPlaying) {
//                    setText("Stop playing")
//                } else {
//                    setText("Start playing")
//                }
//                mStartPlaying = !mStartPlaying
//            }
//        }
//
//        init {
//            setText("Start playing")
//            setOnClickListener(clicker)
//        }
//    }

    public override fun onStop() {
        super.onStop()
        if (mRecorder != null) {
            mRecorder!!.release()
            mRecorder = null
        }

        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
    }
}
