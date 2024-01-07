package com.example.musicApp.service;

import com.example.musicApp.dto.ArtistDto;
import com.example.musicApp.dto.ArtistListingDto;
import com.example.musicApp.model.Artist;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ArtistService {

    Artist signUp(Artist artist);

    Artist login(Artist artist);

    Artist updatePassword(String username,String oldPassword, String newPassword);

    ResponseEntity<byte[]> updateImg(Integer user_id, MultipartFile imgFile) throws IOException;

    Iterable<ArtistListingDto> searchArtists(String query);

    ArtistDto getArtistById(Integer id);

    Iterable<Artist> getArtists();
}