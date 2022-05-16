/*
 * Copyright 2022 Punch Through Design LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package si.uni_lj.fe.tnuv.smartslippers

class UserActivity(activityName: String) {
    val activityName : String = activityName

    var startTime : Long = 0L
    var elapsedTime : Long = 0L // Time elapsed in this period of this activity
    var fullTime : Long = 0L // Total elapsed time in this activity
    var currentTime : Long = 0L // Current total elapsed time in the state

    fun start(): Long {
        startTime = System.currentTimeMillis()
        return startTime
    }

    fun stop(): Long {
        if (startTime != 0L){
            elapsedTime = System.currentTimeMillis() - startTime
            fullTime += elapsedTime
            startTime = 0L
        }
        else{
            elapsedTime = 0L;
        }


        return elapsedTime

    }

    fun current(): String {
        if (startTime != 0L) {
            currentTime = fullTime + (System.currentTimeMillis() - startTime)
        } else if (startTime == 0L) {
            currentTime = fullTime
        }

        var seconds = currentTime / 1000
        var minutes = seconds / 60
        var hours = minutes / 60
        var secondsLeft = seconds - (60 * minutes)
        val minutesLeft = minutes - (60 * hours)

        return "${hours}h ${minutesLeft}min ${secondsLeft}s"
        //return fullTime
    }



}