/*
 * Copyright 2019 Punch Through Design LLC
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

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkPermission
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_scan_result.view.device_name
import kotlinx.android.synthetic.main.row_scan_result.view.mac_address
import kotlinx.android.synthetic.main.row_scan_result.view.signal_strength
import java.security.AccessController
import si.uni_lj.fe.tnuv.smartslippers.ConnectionActivity as ConnectionActivity
import java.security.AccessController.checkPermission as checkPermission1

//import org.jetbrains.anko.layoutInflater
private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 9999
class ScanResultAdapter(
    private val items: List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {
    private lateinit var deviceName : TextView
    private lateinit var macAddres : TextView
    private lateinit var signalStrength: TextView




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        Log.i("ADA", "biniding")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_scan_result, parent, false)
        val context = view.context
        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.bind(item)
    }

    class ViewHolder(
        private val view: View,
        private val onClickListener: ((device: ScanResult) -> Unit)
    ) : RecyclerView.ViewHolder(view) {

        private val isLocationPermissionGranted
            get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        private fun hasPermission(permissionType: String): Boolean {
            return ContextCompat.checkSelfPermission(view.context, permissionType) ==
                    PackageManager.PERMISSION_GRANTED
        }

        fun bind(result: ScanResult) {

            if (ContextCompat.checkSelfPermission(
                    view.context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (ContextCompat.checkSelfPermission(view.context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    {
                        ActivityCompat.requestPermissions(ConnectionActivity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }
                Log.i("ADA", "No ble on...")
            } else {
                view.device_name.text = result.device.name ?: "Unnamed"
                view.mac_address.text = result.device.address
                view.signal_strength.text = "${result.rssi} dBm"
                view.setOnClickListener { onClickListener.invoke(result) }
                //}


            }
        }


    }
}
