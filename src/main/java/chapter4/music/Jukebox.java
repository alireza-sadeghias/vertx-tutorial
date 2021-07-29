package chapter4.music;

import chapter3.HttpServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Jukebox extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(Jukebox.class);
    private enum State {PLAYING,PAUESD}
    private State currentMode =State.PAUESD;
    private final Queue<String> playlist = new ArrayDeque<>();
    private final Set<HttpServerResponse> streamers = new HashSet<>();
    private AsyncFile currentFile;
    private long positionInFile;


    @Override
    public void start() throws Exception {
        EventBus eventBus  = vertx.eventBus();
        eventBus.consumer("jukebox.list",this::list);
        eventBus.consumer("jukebox.schedule",this::schedule);
        eventBus.consumer("jukebox.play",this::play);
        eventBus.consumer("jukebox.pause",this::pause);

        vertx.createHttpServer().requestHandler(this::httpHandler).listen(8080);

        vertx.setPeriodic(100,this::streamAudioChunk);
    }

    private void streamAudioChunk(Long aLong) {
        if(currentMode==State.PAUESD){
            return;
        }
        if(currentFile==null && playlist.isEmpty()){
            currentMode=State.PAUESD;
            return;
        }
        if(currentFile==null){
            openNextFile();
        }
        currentFile.read(Buffer.buffer(4096),0,positionInFile,4096,ar->{
           if(ar.succeeded()){
               processReadBuffer(ar.result());
           } else {
               logger.error("Read failed",ar.cause());
               closeCurrentFile();
           }
        });
    }

    private void openNextFile() {
        OpenOptions opts= new OpenOptions().setRead(true);
        currentFile = vertx.fileSystem().openBlocking("tracks/"+playlist.poll(),opts);
        positionInFile=0;
    }

    private void closeCurrentFile() {
        positionInFile = 0 ;
        currentFile.close() ;
        currentFile = null ;
    }

    private void processReadBuffer(Buffer buffer) {
        positionInFile += buffer.length();
        if(buffer.length()==0){
            closeCurrentFile();
            return;
        }
        for (HttpServerResponse streamer : streamers){
            if(!streamer.writeQueueFull()){
                streamer.write(buffer.copy());
            }
        }
    }

    private void httpHandler(HttpServerRequest request) {
        if("/".equals(request.path())){
            openAudioStream(request);
        }
        if(request.path().startsWith("/download/")){
            String santizedPath = request.path().substring(10).replaceAll("/","");
            download(santizedPath,request);
            return;
        }
        request.response().setStatusCode(404).end();
    }

    private void download(String path, HttpServerRequest request) {
        String file ="tracks/"+path;
        if(!vertx.fileSystem().existsBlocking(file)){
            request.response().setStatusCode(404).end();
            return;
        }
        OpenOptions opts = new OpenOptions().setRead(true);
        vertx.fileSystem().open(file,opts,ar->{
           if(ar.succeeded()){
               downloadFile(ar.result(),request);
           } else{
               logger.error("Read faild",ar.cause());
               request.response().setStatusCode(500).end();
           }
        });
    }

    private void downloadFile(AsyncFile file, HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.setStatusCode(200)
                .putHeader("Content-Type","audio/mpeg")
                .setChunked(true);

        file.pipeTo(response);
//        file.handler(buffer->{
//           response.write(buffer);
//           if(response.writeQueueFull()){
//               file.pause();
//               response.drainHandler(v->file.resume());
//           }
//           file.endHandler(v->response.end());
//        });
    }

    private void openAudioStream(HttpServerRequest request) {
        HttpServerResponse response = request
                .response()
                .putHeader("Content-Type","audio/mpeg")
                .setChunked(true);
        streamers.add(response);
        response.endHandler(v->{
           streamers.remove(response);
           logger.info("A streamer left");
        });
    }

    private void pause(Message<?> tMessage) {
        currentMode = State.PAUESD;
    }

    private void play(Message<?> tMessage) {
        currentMode = State.PLAYING;
    }

    private void schedule(Message<JsonObject> request) {
        String file = request.body().getString("file");
        if(playlist.isEmpty() && currentMode == State.PAUESD){
            currentMode = State.PLAYING;
        }
        playlist.offer(file);
    }

    private void list(Message<?> request) {
        vertx.fileSystem().readDir("tracks",".*mp3$",ar->{
           if(ar.succeeded()){
               List<String> files = ar
                       .result()
                       .stream()
                       .map(File::new)
                       .map(File::getName)
                       .collect(Collectors.toList());

               JsonObject jsonObject = new JsonObject().put("files",new JsonArray(files));
               request.reply(jsonObject);

           } else {
               logger.error("readDir failed",ar.cause());
               request.fail(500,ar.cause().getMessage());
           }
        });
    }

}
