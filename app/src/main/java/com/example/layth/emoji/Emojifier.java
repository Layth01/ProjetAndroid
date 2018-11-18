package com.example.layth.emoji;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {

    private static final String  LOG_TAG = Emojifier.class.getSimpleName();

    private static final float EMOJI_SCALE_FACTOR = .9f;
    private static final double SMILE_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;

    static Bitmap detectFacesandOverlayEmoji(Context context, Bitmap picture){

        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        Frame frame = new Frame.Builder().setBitmap(picture).build();

        SparseArray<Face> faces = detector.detect(frame);

        Log.d(LOG_TAG,"detect faces: number of faces ="+faces.size());

        Bitmap resultBitmap = picture;

        if(faces.size()==0){
            Toast.makeText(context, R.string.no_faces_message,Toast.LENGTH_SHORT).show();
        }else {
            for (int i=0; i <faces.size();i++){
                Face face = faces.valueAt(i);

                Bitmap emojiBitmap;
                switch (whichEmoji(face)){
                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_frown);
                        break;
                     default:
                         emojiBitmap = null;
                         Toast.makeText(context,R.string.no_emoji,Toast.LENGTH_SHORT).show();
                }
                resultBitmap = addBitmapToFace(resultBitmap,emojiBitmap,face);
            }
        }
        detector.release();
        return resultBitmap;
    }

    private static Emoji whichEmoji(Face face){

        Log.d(LOG_TAG,"Which emoji: smilling prob"+ face.getIsSmilingProbability());
        Log.d(LOG_TAG,"Which emoji; leftEyeOpen Prob"+ face.getIsLeftEyeOpenProbability());
        Log.d(LOG_TAG,"Which emoji; rightEyeOpen Prob "+ face.getIsRightEyeOpenProbability());

        boolean smile = face.getIsSmilingProbability() > SMILE_PROB_THRESHOLD;
        boolean leftEyeClosed = face.getIsLeftEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        boolean rightEyeClosed = face.getIsRightEyeOpenProbability() <  EYE_OPEN_PROB_THRESHOLD;

        Emoji emoji;
        if(smile){
            if(leftEyeClosed && !rightEyeClosed){
                emoji = Emoji.LEFT_WINK;
            }else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK;
            }else if(leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_SMILE;
            }else {
                emoji = Emoji.SMILE;
            }
        }else {
            if(leftEyeClosed && !rightEyeClosed){
                emoji = Emoji.LEFT_WINK_FROWN;
            }else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK_FROWN;
            }else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_FROWN;
            }else {
                emoji = Emoji.FROWN;
            }
        }

        Log.d(LOG_TAG,"whichEmoji: "+ emoji.name());
        return emoji;
    }

    private static  Bitmap addBitmapToFace(Bitmap backgroundBitmap,Bitmap emojiBitmap,Face face){
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(),backgroundBitmap.getConfig());

        float scaleFactor = EMOJI_SCALE_FACTOR;

        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeigt = (int) (emojiBitmap.getHeight()*
                                    newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);

        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap,newEmojiWidth,newEmojiHeigt,false);

        float emojiPositionX = (face.getPosition().x + face.getWidth()/2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY = (face.getPosition().y + face.getHeight()/2)- emojiBitmap.getHeight() /3;

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap,0,0,null);
        canvas.drawBitmap(emojiBitmap,emojiPositionX,emojiPositionY,null);

        return resultBitmap;
    }

    private enum Emoji{
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN
    }
}
