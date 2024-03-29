package com.example.musicApp.service.impl;

import com.example.musicApp.dto.SongUploadDto;
import com.example.musicApp.dto.SongListingDto;
import com.example.musicApp.enums.OrderEnum;
import com.example.musicApp.model.Artist;
import com.example.musicApp.model.Song;
import com.example.musicApp.repository.ArtistRepository;
import com.example.musicApp.repository.SongRepository;
import com.example.musicApp.service.SongService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Pageable;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class SongServiceImpl implements SongService {
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public SongServiceImpl(SongRepository songRepository, ArtistRepository artistRepository) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
    }

    @Override
    public Song addSong(String username, SongUploadDto details) throws IOException {
        Artist artist = artistRepository.findByUsername(username).get();

        MultipartFile imgFile = details.image();
        String imgFileExtension = imgFile.getOriginalFilename().substring(imgFile.getOriginalFilename().lastIndexOf("."));
        MultipartFile audioFile = details.audio();
        String audioFileExtension = audioFile.getOriginalFilename().substring(audioFile.getOriginalFilename().lastIndexOf("."));

        // create song id
        String songId = UUID.randomUUID().toString();

        // get reference to local folder
        String path = System.getProperty("user.dir");
        // create the new folder
        new File(path + "/songs/" + songId).mkdirs();
        // create the image and audio file
        File local_imgFile = new File(path + "/songs/" + songId + "/" + songId + "-songImg" + imgFileExtension);
        File local_audioFile = new File(path + "/songs/" + songId + "/" + songId + "-songAudio" + audioFileExtension);

        // save the files to the file system
        imgFile.transferTo(local_imgFile);
        audioFile.transferTo(local_audioFile);

        // create the new song object
        Song song = Song.builder()
                .id(songId)
                .title(details.title())
                .imgUrl("http://localhost:8080/download/song/img/" + songId + "-songImg" + imgFileExtension)
                .localImgUrl(local_imgFile.getAbsoluteFile().toString())
                .audioUrl("http://localhost:8080/download/song/audio/" + songId + "-songAudio" + audioFileExtension)
                .localAudioUrl(local_audioFile.getAbsoluteFile().toString())
                .author(artist)
                .downloads(0)
                .favourites(0)
                .listens(0)
                .released(new Date().getTime())
                .build();

        Set<Song> sources = new HashSet<>(songRepository.findAllById(details.source_ids().orElse(new ArrayList<>())));

        song.setSources(sources);

        return songRepository.save(song);
    }

    @Override
    public Iterable<Song> getSongs(Integer pageNo, Integer pageSize, String orderBy, OrderEnum order) {
        if (order == OrderEnum.ASC) {
            return songRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by(orderBy).ascending()));
        } else {
            return songRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by(orderBy).descending()));
        }
    }

    @Override
    public Song getSongByImgUrl(String imgUrl) {
        return songRepository.findByImgUrl(imgUrl).orElse(null);
    }

    @Override
    public Song getSongByAudioUrl(String audioUrl) {
        return songRepository.findByAudioUrl(audioUrl).orElse(null);
    }

    @Override
    public Song getSong(String id) {
        return songRepository.findById(id).orElse(null);
    }

    @Override
    public Iterable<Song> getAllSongsByArtist(Integer id) {
        Artist artist = artistRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return songRepository.findAllByAuthor(artist);
    }

    @Override
    public Iterable<SongListingDto> searchSongs(String query) { return songRepository.findAllByTitleContainingIgnoreCase(query); }

    @Override
    public Song updateSong(String username, Song song) {
        Song songDb = songRepository.getReferenceById(song.getId());

        if (!songDb.getAuthor().getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        songDb = songRepository.save(song);

        return songDb;
    }

    @Override
    public Song incrementDownload(Song song) {
        // increment download
        song.setDownloads(song.getDownloads() + 1);

        return songRepository.save(song);
    }

    @Override
    public Song incrementFavourite(Song song) {
        // increment download
        song.setFavourites(song.getFavourites() + 1);

        return songRepository.save(song);
    }

    @Override
    public Song incrementListen(Song song) {
        // increment download
        song.setListens(song.getListens() + 1);

        return songRepository.save(song);
    }


}
