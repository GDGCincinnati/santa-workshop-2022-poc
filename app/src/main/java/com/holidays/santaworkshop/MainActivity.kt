package com.holidays.santaworkshop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.holidays.santaworkshop.ui.theme.SantaWorkshopTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2

private const val TAG = "SantaWorkshop"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SantaWorkshopTheme {
                Home(
                    takePictureOnClick = {
                        dispatchTakePictureIntent()
                    },
                )
            }
        }
    }


    lateinit var currentPhotoPath: String
    val IMAGE_CAPTURE_REQUEST_CODE = 1000

    val storageRef = Firebase.storage.reference

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            var file = Uri.fromFile(File(currentPhotoPath))
            val imageRef = storageRef.child("images/${file.lastPathSegment}")
            val uploadTask = imageRef.putFile(file)
// Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.i(TAG, "onFailure")
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                Log.i(TAG, "onSuccess")
                val listRef = storageRef.child("images")
// You'll need to import com.google.firebase.storage.ktx.component1 and
// com.google.firebase.storage.ktx.component2
                listRef.listAll()
                    .addOnSuccessListener { (items, prefixes) ->
                        Log.i(TAG, "success list")

                        items.forEach { item ->
                            // All the items under listRef.
                            println("item $item")
                        }
                    }
                    .addOnFailureListener {
                        // Uh-oh, an error occurred!
                        Log.i(TAG, "failure to list")
                    }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager).also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.i(TAG, "error during creation of file")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.holidays.santaworkshop.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(takePictureOnClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = "Santa Workshop")
            },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ))
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = takePictureOnClick,
                shape = CircleShape,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                content = {
                    Icon(Icons.Default.Add, "Camera")
                },
                containerColor = MaterialTheme.colorScheme.primary)
        },
        content = {
            // A surface container using the 'background' color from the theme
            Surface(modifier = Modifier
                .fillMaxSize()
                .padding(it),
                color = MaterialTheme.colorScheme.surface) {

            }
        })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SantaWorkshopTheme {

    }
}