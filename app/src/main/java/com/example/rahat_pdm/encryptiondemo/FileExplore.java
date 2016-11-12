package com.example.rahat_pdm.encryptiondemo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.SecretKey;

public class FileExplore extends Activity {

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<String>();

    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;

    private static final String TAG = "F_PATH";

    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory() + "");
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;

    ListAdapter adapter;


    SecretKey key;
    byte[] bytesEncrypt;
    byte[] bytesDecrypt;
    VincentFileCrypto simpleCrypto = new VincentFileCrypto(this);
    private final String KEY = "abc";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        loadFileList();

        showDialog(DIALOG_LOAD_FILE);
        Log.d(TAG, path.getAbsolutePath());

    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);

                    return (sel.isDirectory() || (sel.isFile() &&
                            filename.substring(filename.lastIndexOf("."), filename.length()).equals(".mp3")))
                            && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.directory_icon;
                    Log.d("DIRECTORY", fileList[i].file);
                } else {
                    Log.d("FILE", fileList[i].file);
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Back", R.drawable.directory_up);
                fileList = temp;
            }
        } else {
            Log.e(TAG, "path does not exist");
        }

        adapter = new ArrayAdapter<Item>(this,
                android.R.layout.select_dialog_item, android.R.id.text1,
                fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        fileList[position].icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new Builder(this);

        if (fileList == null) {
            Log.e(TAG, "No files loaded");
            dialog = builder.create();
            return dialog;
        }

        switch (id)
        {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        chosenFile = fileList[which].file;
                        File sel = new File(path + "/" + chosenFile);
                        if (sel.isDirectory())
                        {
                            firstLvl = false;

                            // Adds chosen directory to list
                            str.add(chosenFile);
                            fileList = null;
                            path = new File(sel + "");

                            loadFileList();

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(TAG, path.getAbsolutePath());

                        }

                        // Checks if 'up' was clicked
                        else if (chosenFile.equalsIgnoreCase("back") && !sel.exists()) {

                            // present directory removed from list
                            String s = str.remove(str.size() - 1);

                            // path modified to exclude present directory
                            path = new File(path.toString().substring(0,
                                    path.toString().lastIndexOf(s)));
                            fileList = null;

                            // if there are no more directories in the list, then
                            // its the first level
                            if (str.isEmpty()) {
                                firstLvl = true;
                            }
                            loadFileList();

                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                            Log.d(TAG, path.getAbsolutePath());

                        }
                        // File picked
                        else
                        {
                            // Perform action with file picked

                            Bundle extras = getIntent().getExtras();
                            String flag = extras.getString("action");

                            if(flag != null)
                            {
                                if("encrypt".equals(flag))
                                {
                                    doEncrypt(sel, extras.getString("author"));
                                }
                                if(flag.equals("decrypt"))
                                {
                                    doDecrypt(sel, extras.getString("author"));
                                }
                            }


                        }

                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }
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
    private void doDecrypt(File sel, String author)
    {
        byte[] inarray;

        FileInputStream fileInputStream = null;

        byte[] bFile = new byte[(int) sel.length()];

        try
        {
            fileInputStream = new FileInputStream(sel);
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

            //bytesDecrypt = simpleCrypto.decrypt(KEY, inarray);
            //bytesDecrypt = simpleCrypto.decrypt(inarray);

            bytesDecrypt = simpleCrypto.decryptPbkdf2(inarray, Installation.id(this) + "____" + author);


            Toast.makeText(getApplicationContext(), "Decrypted", Toast.LENGTH_SHORT).show();
            //play decrypted audio file.

            playMp3(bytesDecrypt);


        } catch (Exception e) {

            e.printStackTrace();

        }
    }
    public static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }


    private void doEncrypt(File sel, String author)
    {
        try
        {

            String name = sel.getName();
            //InputStream stream = this.getContentResolver().openInputStream(sel.);
            FileInputStream stream = new FileInputStream(sel);
            //Toast.makeText(getApplicationContext(), "Streamed ", Toast.LENGTH_LONG).show();

            Toast.makeText(getApplicationContext(), Installation.id(this) + author, Toast.LENGTH_LONG).show();




            try
            {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(stream.getFD());
                mediaPlayer.prepare();
            }
            catch (IOException e)
            {
                Toast.makeText(getApplicationContext(), "Encryption not possible. File already encrypted", Toast.LENGTH_SHORT).show();
                finish();
                System.exit(0);
            }

            // encrypt audio file send as second argument and corresponding key in first argument.

            //bytesEncrypt = simpleCrypto.encrypt(KEY, getAudioFile(stream));

            //bytesEncrypt = simpleCrypto.encrypt(getAudioFile(stream));

            byte[] salt = VincentFileCrypto.generateSalt();
            key = VincentFileCrypto.deriveKeyPbkdf2(salt, Installation.id(this) + "____" + author); // two keys should be same-- Rahat
            //bytesEncrypt = simpleCrypto.encrypt(getAudioFile(stream), key, salt);


            String test = simpleCrypto.encrypt(getAudioFile(stream), key, salt); // test
            /*byte[] rr = simpleCrypto.decryptPbkdf2(test, "pass");
            Toast.makeText(getApplicationContext(), "Decrypted", Toast.LENGTH_SHORT).show();*/

            //bytesEncrypt = fromBase64(test);   //base64 is the main problem


            Toast.makeText(getApplicationContext(), "Encrypted ", Toast.LENGTH_SHORT).show();

            //Store encrypted file in SD card of your mobile with name vincent.mp3.

            File file = new File(Environment.getExternalStorageDirectory(), name.substring(0, (name.length() - 4)) + "__Encrypted_By_Rahat.mp3");
            // Toast.makeText(getApplicationContext(), "Successful ", Toast.LENGTH_SHORT).show();


           /* OutputStream out = null;
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
            }*/


            ByteArrayInputStream streamInput = new ByteArrayInputStream(test.getBytes());

            FileOutputStream output = new FileOutputStream(file);
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = streamInput.read(buffer)) != -1)
            {
                output.write(buffer, 0, len);

            }

            Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();


           /* FileOutputStream fos = new FileOutputStream(file);

            //Toast.makeText(getApplicationContext(), "Over Successful ", Toast.LENGTH_SHORT).show();


            fos.write(bytesEncrypt);
            fos.close();*/


        }

        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e)
        {

            e.printStackTrace();

        }
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