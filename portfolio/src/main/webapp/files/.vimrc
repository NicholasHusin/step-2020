set t_Co=256
colorscheme Tomorrow-Night  " set colorscheme
syntax enable               " enable syntax processing
set shiftwidth=4            " shift width
set tabstop=4	            " number of visual spaces per TAB
set softtabstop=4           " number of spaces in tab when editing
set expandtab	            " tabs are space
set number                  " show line numbers
set showcmd                 " show command in bottom bar
set cursorline              " highlight current line
filetype indent on          " load filetype-specific indent files
set wildmenu                " visual autocomplete for command menu
set showmatch               " highlight matching [{()}]
set incsearch               " search as characters are entered
set hlsearch                " highlight matches


set mouse=a
if has("mouse_sgr")
    set ttymouse=sgr
else
    set ttymouse=xterm2
end

" move vertically by visual line
nnoremap j gj
nnoremap k gk
