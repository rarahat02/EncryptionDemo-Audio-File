package com.example.rahat_pdm.encryptiondemo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    Button btn_Dec, btn_In;
    EditText edt;

    final int ACTIVITY_CHOOSE_FILE = 1;

    byte[] bytesEncrypt;

    byte[] bytesDecrypt;

    VincentFileCrypto simpleCrypto = new VincentFileCrypto(this);

    Context ctx;

    private final String KEY = "abc";

    String flag ="";


    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ctx = this;


        btn_Dec = (Button) findViewById(R.id.btn_Dec);

        btn_In = (Button) findViewById(R.id.btn_In);

        edt = (EditText) findViewById(R.id.edt);

        //btn_Dec.setOnClickListener(btnDecListner);

        //btn_In.setOnClickListener(btnInListner);


    }

    public void encryptButton(View v)
    {
        Intent intent = new Intent(this, FileExplore.class);
        intent.putExtra("action", "encrypt");
        intent.putExtra("author", edt.getText().toString());
        startActivity(intent);
    }
    public void decryptButton(View v)
    {
        Intent intent = new Intent(this, FileExplore.class);
        intent.putExtra("action", "decrypt");
        intent.putExtra("author", edt.getText().toString());
        startActivity(intent);
    }

/*
    public OnClickListener btnInListner = new OnClickListener() {


        public void onClick(View v)
        {

            flag ="encrypt";
            //searchFile();
            Intent intent = new Intent(this, FileExplore.class);
            intent.putExtra("action", "encrypt");
            startActivity(intent);

        }


    };
*/

    public void searchFile()
    {
        Intent chooseFile;          //intent to file manager
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("audio/mp3");                                 // file may be
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }



    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void doEncrypt(Uri contactUri, String path, String name)
    {
        try
        {
            InputStream stream = this.getContentResolver().openInputStream(contactUri);

            Toast.makeText(getApplicationContext(), "Streamed ", Toast.LENGTH_SHORT).show();

            // encrypt audio file send as second argument and corresponding key in first argument.

            bytesEncrypt = simpleCrypto.encrypt(KEY, getAudioFile(stream));

            Toast.makeText(getApplicationContext(), "Encrypted ", Toast.LENGTH_SHORT).show();

            //Store encrypted file in SD card of your mobile with name vincent.mp3.

            File file = new File(Environment.getExternalStorageDirectory(), name.substring(0, (name.length() - 4)) + "__Encrypted_By_Rahat.mp3");
            Toast.makeText(getApplicationContext(), "Successful ", Toast.LENGTH_SHORT).show();


            OutputStream out = null;
            try
            {
                out = new BufferedOutputStream(new FileOutputStream(file));
                out.write(bytesEncrypt);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
                   /* catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }*/
            catch (Exception e) {

                e.printStackTrace();

            }
            finally
            {
                if (out != null)
                {
                    out.close();
                }
            }
            Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();

                  /*  BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(file));
                    buf.write(bytesEncrypt);
                    buf.close();
                    Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();*/


            FileOutputStream fos = new FileOutputStream(file);

            Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();


            fos.write(bytesEncrypt);
            fos.close();

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) {

            e.printStackTrace();

        }


    }

    private void doDecrypt(Uri contactUri, String path, String name)
    {
        byte[] inarray;

        File file = new File(path, name);
        FileInputStream fileInputStream = null;

        byte[] bFile = new byte[(int) file.length()];

        try
        {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);

            fileInputStream.close();
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        inarray = bFile;


   /* } catch (IOException e) {

        // TODO Auto-generated catch block

        e.printStackTrace();

    }*/

        Toast.makeText(getApplicationContext(), "Processed Array ", Toast.LENGTH_SHORT).show();

        try
        {

            // decrypt the file here first argument is key and second is encrypted file which we get from SD card.

            bytesDecrypt = simpleCrypto.decrypt(KEY, inarray);

            Toast.makeText(getApplicationContext(), "Decrypted", Toast.LENGTH_SHORT).show();
            //play decrypted audio file.

            playMp3(bytesDecrypt);


        } catch (Exception e) {

            e.printStackTrace();

        }
        /*try
        {
            InputStream stream = this.getContentResolver().openInputStream(contactUri);

            Toast.makeText(getApplicationContext(), "Streamed ", Toast.LENGTH_SHORT).show();

            // encrypt audio file send as second argument and corresponding key in first argument.

            bytesEncrypt = simpleCrypto.encrypt(KEY, getAudioFile(stream));

            Toast.makeText(getApplicationContext(), "Encrypted ", Toast.LENGTH_SHORT).show();

            //Store encrypted file in SD card of your mobile with name vincent.mp3.

            File file = new File(Environment.getExternalStorageDirectory(), name.substring(0, (name.length() - 4)) + "__Encrypted_By_Rahat.mp3");
            Toast.makeText(getApplicationContext(), "Successful ", Toast.LENGTH_SHORT).show();


            OutputStream out = null;
            try
            {
                out = new BufferedOutputStream(new FileOutputStream(file));
                out.write(bytesEncrypt);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
                   *//* catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }*//*
            catch (Exception e) {

                e.printStackTrace();

            }
            finally
            {
                if (out != null)
                {
                    out.close();
                }
            }
            Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();

                  *//*  BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(file));
                    buf.write(bytesEncrypt);
                    buf.close();
                    Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();*//*


            FileOutputStream fos = new FileOutputStream(file);

            Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();


            fos.write(bytesEncrypt);
            fos.close();

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) {

            e.printStackTrace();

        }
*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //VincentFileCrypto simpleCrypto = new VincentFileCrypto( this);

        if (requestCode == ACTIVITY_CHOOSE_FILE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri contactUri = data.getData();
                String path = data.getData().getPath();
                String name = getFileName(contactUri);
                Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();


                if(flag.equals("encrypt"))
                {
                    doEncrypt(contactUri, path, name);
                }
                else if(flag.equals("decrypt"))
                {
                    doDecrypt(contactUri, path, name);
                }

            }
        }
    }


    public OnClickListener btnDecListner = new OnClickListener() {


        public void onClick(View v)
        {


            //flag = "decrypt";
           // searchFile();
          /*  try {


                // decrypt the file here first argument is key and second is encrypted file which we get from SD card.

                bytesDecrypt = simpleCrypto.decrypt(KEY, getAudioFileFromSdCard());

                Toast.makeText(getApplicationContext(), "Decrypted", Toast.LENGTH_SHORT).show();
                //play decrypted audio file.

                playMp3(bytesDecrypt);


            } catch (Exception e) {

                e.printStackTrace();

            }*/

        }

    };


    public byte[] getAudioFile(InputStream contactUri) throws FileNotFoundException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = contactUri.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Processed Array ", Toast.LENGTH_SHORT).show();

        return buffer.toByteArray();


    }


    /**
     * This method fetch encrypted file which is save in sd card and convert it in byte array after that this  file will be decrept.
     *
     * @return byte array of encrypted data for decription.
     * @throws FileNotFoundException
     */
    public byte[] getAudioFileFromSdCard() throws FileNotFoundException

    {


        byte[] inarray = null;


        try
        {
            //getting root path where encrypted file is stored.

            File sdcard = Environment.getExternalStorageDirectory();

            File file = new File(sdcard, "demo.mp3");


            //Convert file into array of bytes.

            FileInputStream fileInputStream = null;

            byte[] bFile = new byte[(int) file.length()];

            fileInputStream = new FileInputStream(file);

            fileInputStream.read(bFile);

            fileInputStream.close();

            inarray = bFile;


        } catch (IOException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }

        Toast.makeText(getApplicationContext(), "Processed Array ", Toast.LENGTH_SHORT).show();
        return inarray;

    }



    private void playMp3(byte[] mp3SoundByteArray) {

        try {

            // create temp file that will hold byte array

            File tempMp3 = File.createTempFile("demo", "mp3", getCacheDir());

            tempMp3.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempMp3);

            fos.write(mp3SoundByteArray);

            fos.close();


            // Tried reusing instance of media player

            // but that resulted in system crashes...

            MediaPlayer mediaPlayer = new MediaPlayer();

            FileInputStream fis = new FileInputStream(tempMp3);

            mediaPlayer.setDataSource(fis.getFD());


            mediaPlayer.prepare();

            mediaPlayer.start();

        } catch (IOException ex) {


            ex.printStackTrace();

        }


    }
}