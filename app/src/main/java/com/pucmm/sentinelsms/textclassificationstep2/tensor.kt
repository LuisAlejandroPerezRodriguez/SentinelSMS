// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.pucmm.sentinelsms.textclassificationstep2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pucmm.sentinelsms.R
import org.tensorflow.lite.support.label.Category
import com.pucmm.sentinelsms.textclassificationstep2.helpers.TextClassificationClient

class tensor : AppCompatActivity() {
    lateinit var txtInput: EditText
    lateinit var btnSendText: Button
    lateinit var txtOutput: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.option1)
        val client = TextClassificationClient(applicationContext)
        client.load()
        txtInput = findViewById(R.id.txtInput)
        txtOutput = findViewById(R.id.txtOutput)
        btnSendText = findViewById(R.id.btnSendText)
        btnSendText.setOnClickListener {
            var toSend:String = txtInput.text.toString()
            var results:List<Category> = client.classify(toSend)
            val score = results[1].score
            if(score>0.8){
                txtOutput.text = "Your message was detected as spam with a score of " + score.toString() + " and not sent!"
            } else {
                txtOutput.text = "Message sent! \nSpam score was:" + score.toString()
            }
            //txtInput.text.clear()
        }

    }
}