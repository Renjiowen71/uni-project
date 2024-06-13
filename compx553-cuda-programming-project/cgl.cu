#include <iostream>
#include <random>
#include <chrono>
#include <sys/time.h>
#include <ctime>


using std::cout; using std::endl;
using std::chrono::duration_cast;
using std::chrono::milliseconds;
using std::chrono::seconds;
using std::chrono::system_clock;


/**
* range: the maximum of rows and columns of the board to print 
*/
void print_board(bool *board, int board_size, int range){
   cout << endl;
   for (int row = 0; row < range && row < board_size; row++) {
     for(int col=0; col < range && col < board_size; col++){
           cout << board[col + row*board_size];

     }
     cout<<endl;
   }
    
}

/**
* initialize a board array of board_size*board_size on the host
*
*/
void init_board(bool* board, int board_size){
  //starts with a simple pattern at the left corner region (0,0) to (range, range)
  int range = 128;
  //use a fixed seed for the same board pattern 
  srand(1);
  //get system time in seconds and use it as the random seed for different board patterns       
  //auto sec_since_epoch = duration_cast<seconds>(system_clock::now().time_since_epoch()).count();
  //srand(sec_since_epoch);
  for (int row = 1; row < board_size -1; row++) {
     for(int col= 1; col < board_size -1; col++){
         if(row < range && col < range){
               board[col + row * board_size] = rand()%2;
               continue;                      
        }
       board[col + row * board_size] = 0;
     }
  }
}

/**
*Implemention of the CPU version
*Any live cell with fewer than two live neighbours dies, as if by underpopulation.
*Any live cell with two or three live neighbours lives on to the next generation.
*Any live cell with more than three live neighbours dies, as if by overpopulation.
*Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
*Note: you can ignore the cells on four edges by setting row or col = 1 and row or col < board_size-1
*/
void nextGeneration(bool* board, bool* next_board, int board_size){
     
  for (int row = 1; row < board_size -1; row++) {
    for(int col= 1; col < board_size -1; col++){
      //Calculate live neighbors      
      int live_neighbors = 0;
      int index = row * board_size + col;
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          if (!(i == 0 && j == 0)) {
            if (board[(row + i) * board_size + (col + j)]) {
              live_neighbors++;
            }
          }
        }
      }

      //Conditions necessary for life
      next_board[index] = (board[index] ? (live_neighbors == 2 || live_neighbors == 3) : (live_neighbors == 3));
    }
  }
}
/**
* Implemention of the GPU version without using shared memory
*
*/
__global__ void nextGenerationGPU(bool* board, bool* next_board, int board_size){
  int index = blockIdx.x * blockDim.x + threadIdx.x;
    
  //Calculate Row and Column  
  int row = index / board_size;
  int col = index % board_size;
  
  if (row >= 1 && row < board_size - 1 && col >= 1 && col < board_size - 1) {
    
    //Calculate live neighbors  
    int live_neighbors = board[(row - 1) * board_size + (col - 1)] 
      + board[(row - 1) * board_size + col] 
      + board[(row - 1) * board_size + (col + 1)] 
      + board[row * board_size + (col - 1)] 
      + board[row * board_size + (col + 1)] 
      + board[(row + 1) * board_size + (col - 1)] 
      + board[(row + 1) * board_size + col] 
      + board[(row + 1) * board_size + (col + 1)];

    //Life's condition 
    next_board[index] = (board[index] ? (live_neighbors == 2 || live_neighbors == 3) : (live_neighbors == 3));
    
  }
}

const int TILE_SIZE = 16; // Define the size of the tile/block
/**
* Implemention of the GPU version using shared memory   
*
*/
__global__ void nextGenerationGPUSharedMemory(bool* board, bool* next_board, int board_size){
  __shared__ bool shared_board[TILE_SIZE+2][TILE_SIZE+2];
    
  int col = blockIdx.x * blockDim.x + threadIdx.x;
  int row = blockIdx.y * blockDim.y + threadIdx.y;
  
  int shared_row = threadIdx.y+1;
  int shared_col = threadIdx.x+1;
  
  if (row >= 1 && row < board_size - 1 && col >= 1 && col < board_size - 1) {

    shared_board[shared_row-1][shared_col-1] = board[(row-1) * board_size + (col-1)];

    if (threadIdx.x>=TILE_SIZE-2){
      shared_board[shared_row-1][shared_col+1] = board[(row-1) * board_size + (col+1)];
    }

    if (threadIdx.y>=TILE_SIZE-2){
      shared_board[shared_row+1][shared_col-1] = board[(row+1) * board_size + (col-1)];
    }
    
    if (threadIdx.y>=TILE_SIZE-2&&threadIdx.x>=TILE_SIZE-2){
      shared_board[shared_row+1][shared_col+1] = board[(row+1) * board_size + (col+1)];
    }

    __syncthreads();

    // Calculate next board using shared board
    int live_neighbors = shared_board[shared_row - 1][shared_col - 1] // Top-left
      + shared_board[shared_row - 1][shared_col]     // Top
      + shared_board[shared_row - 1][shared_col + 1] // Top-right
      + shared_board[shared_row][shared_col - 1]     // Left
      + shared_board[shared_row][shared_col + 1]     // Right
      + shared_board[shared_row + 1][shared_col - 1] // Bottom-left
      + shared_board[shared_row + 1][shared_col]     // Bottom
      + shared_board[shared_row + 1][shared_col + 1]; // Bottom-right

    //Life's condition 
    next_board[row * board_size + col] = (shared_board[shared_row][shared_col]  ? (live_neighbors == 2 || live_neighbors == 3) : (live_neighbors == 3));
  }
}

// Conway's Game of Life Test 
void init_block_board(bool* board, int board_size){
  board[board_size+1] = true;
  board[board_size+2] = true;
  board[board_size*2+1] = true;
  board[board_size*2+2] = true;
}
void init_blinker_board(bool* board, int board_size){
  board[board_size+2] = true;
  board[board_size*2+2] = true;
  board[board_size*3+2] = true;
}
void init_glider_board(bool* board, int board_size){
  board[board_size+1] = true;
  board[board_size+3] = true;
  board[board_size*2+2] = true;
  board[board_size*2+3] = true;
  board[board_size*3+2] = true;
}
void init_LWSS_board(bool* board, int board_size){
  board[board_size+3] = true;
  board[board_size+4] = true;
  board[board_size*2+2] = true;
  board[board_size*2+3] = true;
  board[board_size*2+4] = true;
  board[board_size*2+5] = true;
  board[board_size*3+2] = true;
  board[board_size*3+3] = true;
  board[board_size*3+5] = true;
  board[board_size*3+6] = true;
  board[board_size*4+4] = true;
  board[board_size*4+5] = true;
}

int main(void)
{
  int board_size = 32768; //2048,4096,8192,16384,32768
  int print_range = 12;
    
  //Initialize the Board
  dim3 threadsPerBlock(TILE_SIZE, TILE_SIZE);
  dim3 numBlocks((board_size + threadsPerBlock.x - 1) / threadsPerBlock.x, 
                         (board_size + threadsPerBlock.y - 1) / threadsPerBlock.y);
  // int thread_block = 64;
  // int num_blocks = (board_size * board_size + thread_block - 1) / thread_block;
  bool* pre_board = new bool[board_size * board_size];
  bool* next_board = new bool[board_size * board_size];
  bool* d_pre_board;
  bool* d_next_board;
  init_board(pre_board, board_size);
  //init_block_board(pre_board, board_size);
  //init_blinker_board(pre_board,board_size);
  //init_glider_board(pre_board,board_size);
  //init_LWSS_board(pre_board,board_size);

  print_board(pre_board, board_size, print_range);
   
  cudaMalloc((void**)&d_pre_board, sizeof(bool) * board_size * board_size);
  cudaMalloc((void**)&d_next_board, sizeof(bool) * board_size * board_size);

  for (int i=0; i<10;i++){
    //run at least ten generations and measure the elapsed time for each generation
    auto start = std::chrono::high_resolution_clock::now();
      
    //run one generation

    // nextGeneration(pre_board, next_board, board_size);

    // cudaMemcpy(d_pre_board, pre_board, sizeof(bool) * board_size * board_size, cudaMemcpyHostToDevice);
    // nextGenerationGPU<<<num_blocks, thread_block>>>(d_pre_board, d_next_board, board_size);
    // cudaDeviceSynchronize();
    // cudaMemcpy(next_board, d_next_board, sizeof(bool) * board_size * board_size, cudaMemcpyDeviceToHost);

    cudaMemcpy(d_pre_board, pre_board, sizeof(bool) * board_size * board_size, cudaMemcpyHostToDevice);
    nextGenerationGPUSharedMemory<<<numBlocks, threadsPerBlock>>>(d_pre_board, d_next_board, board_size);
    cudaDeviceSynchronize();
    cudaMemcpy(next_board, d_next_board, sizeof(bool) * board_size * board_size, cudaMemcpyDeviceToHost);

    auto end = std::chrono::high_resolution_clock::now();
    auto milliseconds = std::chrono::duration_cast<std::chrono::milliseconds>(end-start);
    std::cout << "Generation " << i+1 << " elapsed time: " << milliseconds.count() << " milliseconds" << std::endl;

    print_board(next_board, board_size, print_range);

    std::swap(pre_board, next_board);
  }

  cudaFree(d_pre_board);
  cudaFree(d_next_board);
  return 0;
}

