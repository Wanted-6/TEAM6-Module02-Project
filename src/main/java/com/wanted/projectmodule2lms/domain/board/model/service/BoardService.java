package com.wanted.projectmodule2lms.domain.board.model.service;


import com.wanted.projectmodule2lms.domain.board.model.dao.BoardRepository;
import com.wanted.projectmodule2lms.domain.board.model.dto.BoardDTO;
import com.wanted.projectmodule2lms.domain.board.model.entity.Board;
import com.wanted.projectmodule2lms.domain.board.model.entity.BoardType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    public List<BoardDTO> findBoardByPostType(BoardType postType){
        List<Board> boardList = boardRepository.findByPostTypeAndIsDeletedFalse(postType);
        return boardList.stream().map(board -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());
    }

    public BoardDTO findBoardById(Integer postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        return modelMapper.map(board, BoardDTO.class);
    }






}
