#pragma once

#include <cmath>
#include <iostream>
#include <stdexcept>

// ------------------------------------
// START OF INTERFACE
// ------------------------------------

template< typename T >
class BucketStorage
{
  public:
	using value_type = T;
	using size_type = std::size_t;
	using difference_type = std::ptrdiff_t;
	using reference = value_type &;
	using const_reference = const value_type &;
	using pointer = value_type *;

	class iterator;

	class const_iterator;

	explicit BucketStorage(size_type m_block_capacity = 64);

	BucketStorage(const BucketStorage &other);

	BucketStorage(BucketStorage &&other) noexcept;

	~BucketStorage();

	BucketStorage &operator=(const BucketStorage &other) noexcept;

	BucketStorage &operator=(BucketStorage &&other) noexcept;

	iterator insert(const value_type &value);

	iterator insert(value_type &&value);

	iterator erase(iterator it);

	bool empty() const noexcept;

	size_type size() const noexcept;

	size_type capacity() const noexcept;

	void shrink_to_fit();

	void clear();

	void swap(BucketStorage &other) noexcept;

	iterator begin() noexcept;

	const_iterator begin() const noexcept;

	const_iterator cbegin() const noexcept;

	iterator end() noexcept;

	const_iterator end() const noexcept;

	const_iterator cend() const noexcept;

	iterator get_to_distance(iterator it, difference_type distance) noexcept;

  private:
	class List
	{
	  private:
		class Node
		{
			friend class List;

		  public:
			explicit Node(size_type value) : m_node_data(value), m_next(nullptr) {}

		  private:
			size_type m_node_data;
			Node *m_next;
		};

		Node *m_head;
		size_type m_size;

	  public:
		List() noexcept : m_head(nullptr), m_size(0) {}

		List(const List &other) noexcept;

		List(List &&other) noexcept;

		~List();

		void append(size_type value);

		size_type pop();

		size_type size() const noexcept { return m_size; }

		friend class BucketStorage;
	};

	class Block
	{
		pointer m_data = nullptr;
		bool *m_active = nullptr;
		size_type m_capacity;
		size_type m_size;

		size_type m_index;

		List *m_freeIndex = nullptr;

		explicit Block(size_type capacity = 64);

		Block &operator=(Block &&other) noexcept;

		~Block();

		template< typename U >
		size_type append(U &&item);

		void remove(iterator it);

		friend class BucketStorage;

		friend class iterator;

		friend class const_iterator;
	};

	size_type m_block_capacity;
	size_type m_block_count;
	size_type m_block_index;
	Block *m_blocks;
	List *m_freeBlock;
	size_type m_size;

	void allocate_new_block();
	void deallocate_block(Block *block);
	template< typename U >
	iterator insert_impl(U &&value);
};

template< typename T >
class BucketStorage< T >::iterator
{
  public:
	using value_type = T;
	using difference_type = std::ptrdiff_t;
	using pointer = T *;
	using reference = T &;

	iterator() noexcept : m_storage(nullptr), m_index_block(0), m_index_el(0){};

	iterator(BucketStorage *m_storage, size_type block_ind, size_type index) noexcept :
		m_storage(m_storage), m_index_block(block_ind), m_index_el(index)
	{
	}

	reference operator*() const noexcept;

	pointer operator->() const noexcept;

	iterator &operator++() noexcept;

	iterator operator++(int) noexcept;

	iterator &operator--() noexcept;

	iterator operator--(int) noexcept;

	bool operator<(const iterator &other) const noexcept;

	bool operator>(const iterator &other) const noexcept;

	bool operator<=(const iterator &other) const noexcept;

	bool operator>=(const iterator &other) const noexcept;

	bool operator==(const iterator &other) const noexcept;

	bool operator!=(const iterator &other) const noexcept;

	bool operator==(const const_iterator &other) const noexcept;

	bool operator!=(const const_iterator &other) const noexcept;

  private:
	BucketStorage *m_storage;
	size_type m_index_el;
	size_type m_index_block;

	friend class BucketStorage;
};

template< typename T >
class BucketStorage< T >::const_iterator
{
  public:
	using value_type = T;
	using difference_type = std::ptrdiff_t;
	using pointer = const T *;
	using reference = const T &;

	const_iterator() noexcept : m_storage(nullptr), m_index_block(0), m_index_el(0) {}

	const_iterator(const BucketStorage *storage, size_type block_idx, size_type index) noexcept :
		m_storage(storage), m_index_block(block_idx), m_index_el(index)
	{
	}

	const_iterator(const iterator &it);

	reference operator*() const noexcept;

	pointer operator->() const noexcept;

	const_iterator &operator++() noexcept;

	const_iterator operator++(int) noexcept;

	const_iterator &operator--() noexcept;

	const_iterator operator--(int) noexcept;

	bool operator==(const const_iterator &other) const noexcept;

	bool operator!=(const const_iterator &other) const noexcept;

  private:
	const BucketStorage *m_storage;
	size_type m_index_block;
	size_type m_index_el;

	friend class BucketStorage;
};

// ------------------------------------
// START OF STORAGE IMPLEMENTATION
// ------------------------------------

template< typename T >
BucketStorage< T >::Block::Block(BucketStorage::size_type capacity) : m_capacity(capacity), m_size(0), m_index(0)
{
	try
	{
		m_data = static_cast< pointer >(operator new(capacity * sizeof(value_type)));
		m_active = new bool[capacity]{ false };
		m_freeIndex = new List();
	} catch (std::bad_alloc &e)
	{
		this->~Block();
	}
}

template< typename T >
typename BucketStorage< T >::Block &BucketStorage< T >::Block::operator=(BucketStorage::Block &&other) noexcept
{
	if (this != &other)
	{
		this->~Block();

		m_data = other.m_data;
		m_active = other.m_active;
		m_capacity = other.m_capacity;
		m_size = other.m_size;
		m_index = other.m_index;
		m_freeIndex = std::move(other.m_freeIndex);

		other.m_data = nullptr;
		other.m_active = nullptr;
		other.m_capacity = 0;
		other.m_size = 0;
		other.m_index = 0;
		other.m_freeIndex = nullptr;
	}
	return *this;
}

template< typename T >
BucketStorage< T >::Block::~Block()
{
	for (size_type i = 0; i < m_capacity; ++i)
	{
		if (m_active[i])
		{
			m_data[i].~T();
		}
	}

	delete[] m_active;
	operator delete(m_data);
	delete m_freeIndex;
}

template< typename T >
template< typename U >
typename BucketStorage< T >::size_type BucketStorage< T >::Block::append(U &&item)
{
	size_type index;
	if (m_freeIndex->size())
	{
		index = m_freeIndex->pop();
	}
	else
	{
		index = m_index++;
	}
	new (&m_data[index]) value_type(std::forward< U >(item));
	m_active[index] = true;
	m_size++;
	return index;
}

template< typename T >
void BucketStorage< T >::Block::remove(BucketStorage::iterator it)
{
	size_type block_idx = it.m_index_block;
	size_type elem_idx = it.m_index_el;
	if (it.m_storage->m_blocks[block_idx].m_active[elem_idx])
	{
		it.m_storage->m_blocks[block_idx].m_data[elem_idx].~T();
		it.m_storage->m_blocks[block_idx].m_freeIndex->append(elem_idx);
		it.m_storage->m_freeBlock->append(block_idx);
		it.m_storage->m_blocks[block_idx].m_size--;
		it.m_storage->m_blocks[block_idx].m_active[elem_idx] = false;
	}
}

template< typename T >
BucketStorage< T >::List::List(const BucketStorage< T >::List &other) noexcept : m_head(nullptr), m_size(other.m_size)
{
	if (other.m_head == nullptr)
	{
		return;
	}
	Node *other_current = other.m_head;

	while (other_current != nullptr)
	{
		append(other_current->m_node_data);
		other_current = other_current->m_next;
	}
}

template< typename T >
BucketStorage< T >::List::List(BucketStorage< T >::List &&other) noexcept : m_head(other.m_head), m_size(other.m_size)
{
	other.m_head = nullptr;
	other.m_size = 0;
}

template< typename T >
BucketStorage< T >::List::~List()
{
	Node *current = m_head;
	while (current != nullptr)
	{
		Node *next = current->m_next;
		delete current;
		current = next;
	}
	m_head = nullptr;
	m_size = 0;
}

template< typename T >
void BucketStorage< T >::List::append(size_type value)
{
	Node *newNode;
	try
	{
		newNode = new Node(value);
		if (m_head == nullptr)
		{
			m_head = newNode;
		}
		else
		{
			Node *temp = m_head;
			while (temp->m_next != nullptr)
			{
				temp = temp->m_next;
			}
			temp->m_next = newNode;
		}
		m_size++;
	} catch (std::bad_alloc &e)
	{
		delete newNode;
	}
}

template< typename T >
typename BucketStorage< T >::size_type BucketStorage< T >::List::pop()
{
	if (m_head == nullptr)
	{
		throw std::out_of_range("Error list is empty");
	}

	Node *temp = m_head;
	size_type value = temp->m_node_data;
	m_head = m_head->m_next;
	delete temp;
	m_size--;

	return value;
}

template< typename T >
BucketStorage< T >::BucketStorage(BucketStorage::size_type m_block_capacity) :
	m_block_capacity(m_block_capacity), m_size(0), m_block_index(0), m_block_count(0), m_blocks(nullptr)
{
	if (m_block_capacity < 1)
	{
		throw std::invalid_argument("Block capacity must be greater than 0");
	}
	allocate_new_block();
	try
	{
		m_freeBlock = new List();
	} catch (std::bad_alloc &e)
	{
		this->~BucketStorage();
	}
}

template< typename T >
BucketStorage< T >::BucketStorage(const BucketStorage &other) :
	m_block_capacity(other.m_block_capacity), m_size(0), m_block_index(0), m_block_count(0), m_blocks(nullptr)
{
	m_freeBlock = new List(*other.m_freeBlock);
	for (const auto &value : other)
	{
		insert(value);
	}
}

template< typename T >
BucketStorage< T >::BucketStorage(BucketStorage &&other) noexcept :
	m_block_capacity(other.m_block_capacity), m_blocks(other.m_blocks), m_size(other.m_size),
	m_block_index(other.m_block_index), m_block_count(other.m_block_count)
{
	other.m_blocks = nullptr;
	m_freeBlock = std::move(other.m_freeBlock);
	other.m_freeBlock = nullptr;
	other.m_size = 0;
	other.m_block_capacity = 0;
	other.m_block_count = 0;
}

template< typename T >
BucketStorage< T >::~BucketStorage()
{
	delete[] m_blocks;
	delete m_freeBlock;
	m_size = 0;
	m_block_count = 0;
}

template< typename T >
BucketStorage< T > &BucketStorage< T >::operator=(const BucketStorage &other) noexcept
{
	if (this != &other)
	{
		BucketStorage< T > temp(other);
		swap(temp);
	}
	return *this;
}

template< typename T >
BucketStorage< T > &BucketStorage< T >::operator=(BucketStorage &&other) noexcept
{
	if (this != &other)
	{
		clear();
		swap(other);
	}
	return *this;
}

template< typename T >
void BucketStorage< T >::allocate_new_block()
{
	try
	{
		auto *new_blocks = new Block[m_block_count + 1]();
		if (m_size)
		{
			for (size_type i = 0; i < m_block_count; ++i)
			{
				new_blocks[i] = std::move(m_blocks[i]);
			}
			delete[] m_blocks;
		}
		m_block_count++;
		m_blocks = new_blocks;
	} catch (std::bad_alloc &e)
	{
		this->~BucketStorage();
	}
}

template< typename T >
template< typename U >
typename BucketStorage< T >::iterator BucketStorage< T >::insert_impl(U &&value)
{
	if (m_size < m_block_capacity * m_block_count)
	{
		size_type block_index;
		if (m_freeBlock != nullptr && m_freeBlock->m_size > 0)
		{
			block_index = m_freeBlock->pop();
		}
		else
		{
			block_index = m_block_index;
		}
		size_type block_elem = m_blocks[block_index].append(std::forward< U >(value));
		m_size++;
		return iterator(this, block_index, block_elem);
	}
	else
	{
		allocate_new_block();
		m_block_index = m_block_count - 1;
		return insert_impl(std::forward< U >(value));
	}
}

template< typename T >
void BucketStorage< T >::deallocate_block(Block *block)
{
	size_type block_idx = block - m_blocks;

	--m_block_count;

	if (m_block_count > 0)
	{
		auto *new_blocks = new Block[m_block_count];
		for (size_type i = 0, j = 0; i < m_block_count + 1; ++i)
		{
			if (i != block_idx)
			{
				new_blocks[j++] = std::move(m_blocks[i]);
			}
		}
		delete[] m_blocks;
		m_blocks = new_blocks;
	}
	else
	{
		delete[] m_blocks;
		m_blocks = nullptr;
	}
}

template< typename T >
typename BucketStorage< T >::iterator::reference BucketStorage< T >::iterator::operator*() const noexcept
{
	return m_storage->m_blocks[m_index_block].m_data[m_index_el];
}

template< typename T >
typename BucketStorage< T >::iterator::pointer BucketStorage< T >::iterator::operator->() const noexcept
{
	return &m_storage->m_blocks[m_index_block].m_data[m_index_el];
}

template< typename T >
typename BucketStorage< T >::iterator &BucketStorage< T >::iterator::operator++() noexcept
{
	if (m_storage == nullptr)
		return *this;

	while (m_index_block < m_storage->m_block_count)
	{
		if (m_index_el < m_storage->m_block_capacity - 1)
		{
			++m_index_el;
			if (m_storage->m_blocks[m_index_block].m_active[m_index_el])
			{
				return *this;
			}
		}
		else
		{
			++m_index_block;
			m_index_el = 0;
			if (m_index_block < m_storage->m_block_count)
			{
				if (m_storage->m_blocks[m_index_block].m_active[m_index_el])
				{
					return *this;
				}
			}
		}
	}
	*this = m_storage->end();
	return *this;
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::iterator::operator++(int) noexcept
{
	iterator temp = *this;
	++(*this);
	return temp;
}

template< typename T >
typename BucketStorage< T >::iterator &BucketStorage< T >::iterator::operator--() noexcept
{
	if (m_storage == nullptr)
		return *this;

	if (*this == m_storage->begin())
	{
		return *this;
	}
	if (*this == m_storage->end())
	{
		m_index_block--;
		m_index_el--;
		return *this;
	}
	while (m_index_block < m_storage->m_block_count)
	{
		if (m_index_el > 0)
		{
			--m_index_el;
			if (m_storage->m_blocks[m_index_block].m_active[m_index_el])
			{
				return *this;
			}
		}
		else
		{
			if (m_index_block > 0)
			{
				--m_index_block;
				m_index_el = m_storage->m_blocks[m_index_block].m_capacity;
			}
			else
			{
				*this = m_storage->begin();
				return *this;
			}
		}
	}

	return *this;
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::iterator::operator--(int) noexcept
{
	iterator temp = *this;
	--(*this);
	return temp;
}

template< typename T >
bool BucketStorage< T >::iterator::operator<(const BucketStorage< T >::iterator &other) const noexcept
{
	return m_storage == other.m_storage &&
		   (m_index_block < other.m_index_block || (m_index_block == other.m_index_block && m_index_el < other.m_index_el));
}

template< typename T >
bool BucketStorage< T >::iterator::operator>(const BucketStorage< T >::iterator &other) const noexcept
{
	return other < *this;
}

template< typename T >
bool BucketStorage< T >::iterator::operator<=(const BucketStorage< T >::iterator &other) const noexcept
{
	return !(other < *this);
}

template< typename T >
bool BucketStorage< T >::iterator::operator>=(const BucketStorage< T >::iterator &other) const noexcept
{
	return !(*this < other);
}

template< typename T >
bool BucketStorage< T >::iterator::operator==(const BucketStorage< T >::iterator &other) const noexcept
{
	return m_storage == other.m_storage && m_index_block == other.m_index_block && m_index_el == other.m_index_el;
}

template< typename T >
bool BucketStorage< T >::iterator::operator!=(const BucketStorage< T >::iterator &other) const noexcept
{
	return !(*this == other);
}

template< typename T >
bool BucketStorage< T >::iterator::operator==(const BucketStorage< T >::const_iterator &other) const noexcept
{
	return m_storage == other.m_storage && m_index_block == other.m_index_block && m_index_el == other.m_index_el;
}

template< typename T >
bool BucketStorage< T >::iterator::operator!=(const BucketStorage< T >::const_iterator &other) const noexcept
{
	return !(*this == other);
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::insert(const value_type &value)
{
	return BucketStorage::insert_impl(value);
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::insert(value_type &&value)
{
	return BucketStorage::insert_impl(std::move(value));
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::begin() noexcept
{
	for (size_type block = 0; block < m_block_count; ++block)
	{
		for (size_type elem = 0; elem < m_block_capacity; ++elem)
		{
			if (m_blocks[block].m_active[elem])
			{
				return iterator(this, block, elem);
			}
		}
	}
	return end();
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::end() noexcept
{
	return iterator(this, m_block_count, (m_size % m_block_capacity) + m_freeBlock->size());
}

template< typename T >
typename BucketStorage< T >::const_iterator BucketStorage< T >::begin() const noexcept
{
	for (size_type block = 0; block < m_block_count; ++block)
	{
		for (size_type elem = 0; elem < m_block_capacity; ++elem)
		{
			if (m_blocks[block].m_active[elem])
			{
				return const_iterator(this, block, elem);
			}
		}
	}
	return end();
}

template< typename T >
typename BucketStorage< T >::const_iterator BucketStorage< T >::cbegin() const noexcept
{
	return begin();
}

template< typename T >
typename BucketStorage< T >::const_iterator BucketStorage< T >::end() const noexcept
{
	return const_iterator(this, m_block_count, (m_size % m_block_capacity) + m_freeBlock->size());
}

template< typename T >
typename BucketStorage< T >::const_iterator BucketStorage< T >::cend() const noexcept
{
	return end();
}

// ------------------------------------
// START OF CONST_ITERATOR IMPLEMENTATION
// ------------------------------------

template< typename T >
BucketStorage< T >::const_iterator::const_iterator(const BucketStorage< T >::iterator &it) :
	m_storage(it.m_storage), m_index_block(it.m_index_block), m_index_el(it.m_index_el)
{
}

template< typename T >
typename BucketStorage< T >::const_iterator::reference BucketStorage< T >::const_iterator::operator*() const noexcept
{
	return m_storage->m_blocks[m_index_block].m_data[m_index_el];
}

template< typename T >
typename BucketStorage< T >::const_iterator::pointer BucketStorage< T >::const_iterator::operator->() const noexcept
{
	if (!m_storage->m_blocks[m_index_block].m_active[m_index_el])
	{
		throw std::out_of_range("Dereference an invalid iterator");
	}
	return &m_storage->m_blocks[m_index_block].m_data[m_index_el];
}

template< typename T >
typename BucketStorage< T >::const_iterator &BucketStorage< T >::const_iterator::operator++() noexcept
{
	if (m_storage == nullptr)
		return *this;

	while (m_index_block < m_storage->m_block_count)
	{
		if (m_index_el < m_storage->m_block_capacity - 1)
		{
			++m_index_el;
			if (m_storage->m_blocks[m_index_block].m_active[m_index_el])
			{
				return *this;
			}
		}
		else
		{
			++m_index_block;
			m_index_el = 0;
			if (m_index_block < m_storage->m_block_count)
			{
				if (m_storage->m_blocks[m_index_block].m_active[m_index_el])
				{
					return *this;
				}
			}
		}
	}
	*this = m_storage->cend();
	return *this;
}

template< typename T >
typename BucketStorage< T >::const_iterator BucketStorage< T >::const_iterator::operator++(int) noexcept
{
	const_iterator temp = *this;
	++(*this);
	return temp;
}

template< typename T >
typename BucketStorage< T >::const_iterator &BucketStorage< T >::const_iterator::operator--() noexcept
{
	if (m_storage == nullptr)
		return *this;

	if (*this == m_storage->cbegin())
	{
		return *this;
	}

	while (m_index_block < m_storage->m_block_count)
	{
		if (m_index_el > 0)
		{
			--m_index_el;
			if (m_storage->m_blocks[m_index_block].m_active[m_index_el])
			{
				return *this;
			}
		}
		else
		{
			if (m_index_block > 0)
			{
				--m_index_block;
				m_index_el = m_storage->m_blocks[m_index_block].m_capacity;
			}
			else
			{
				*this = m_storage->cbegin();
				return *this;
			}
		}
	}

	return *this;
}

template< typename T >
typename BucketStorage< T >::const_iterator BucketStorage< T >::const_iterator::operator--(int) noexcept
{
	const_iterator temp = *this;
	--(*this);
	return temp;
}

template< typename T >
bool BucketStorage< T >::const_iterator::operator==(const BucketStorage< T >::const_iterator &other) const noexcept
{
	return m_storage == other.m_storage && m_index_block == other.m_index_block && m_index_el == other.m_index_el;
}

template< typename T >
bool BucketStorage< T >::const_iterator::operator!=(const BucketStorage< T >::const_iterator &other) const noexcept
{
	return !(*this == other);
}

template< typename T >
typename BucketStorage< T >::iterator BucketStorage< T >::erase(BucketStorage< T >::iterator it)
{
	if (it.m_storage != this || it.m_index_block >= m_block_count || it.m_index_el >= m_block_capacity)
	{
		throw std::out_of_range("Invalid iterator");
	}

	m_blocks[it.m_index_block].remove(it);
	--m_size;
	if (m_blocks[it.m_index_block].m_size == 0)
	{
		deallocate_block(&m_blocks[it.m_index_block]);
	}
	auto next = it;
	++next;

	return next;
}

template< typename T >
bool BucketStorage< T >::empty() const

	noexcept
{
	return m_size == 0;
}

template< typename T >
typename BucketStorage< T >::size_type BucketStorage< T >::size() const noexcept
{
	return m_size;
}

template< typename T >
typename BucketStorage< T >::size_type BucketStorage< T >::capacity() const noexcept
{
	if (!m_size)
		return 0;
	return (m_block_count * m_block_capacity);
}

template< typename T >
void BucketStorage< T >::shrink_to_fit()
{
	if (size() < capacity())
	{
		m_block_capacity = m_size + 2;
		BucketStorage tmp(*this);
		delete[] m_blocks;
		delete m_freeBlock;
		m_size = 0;
		m_block_count = 0;
		m_freeBlock = new List();

		for (const auto &item : tmp)
		{
			insert(item);
		}
	}
}

template< typename T >
void BucketStorage< T >::clear()
{
	delete[] m_blocks;
	delete m_freeBlock;
	m_size = 0;
	m_block_count = 0;
	m_freeBlock = new List();
	allocate_new_block();
}

template< typename T >
void BucketStorage< T >::swap(BucketStorage &other) noexcept
{
	std::swap(m_block_capacity, other.m_block_capacity);
	std::swap(m_blocks, other.m_blocks);
	std::swap(m_freeBlock, other.m_freeBlock);
	std::swap(m_block_count, other.m_block_count);
	std::swap(m_block_index, other.m_block_index);
	std::swap(m_size, other.m_size);
}

template< typename T >
typename BucketStorage< T >::iterator
	BucketStorage< T >::get_to_distance(BucketStorage::iterator it, BucketStorage::difference_type distance) noexcept
{
	while (distance > 0)
	{
		++it;
		--distance;
	}
	while (distance < 0)
	{
		--it;
		++distance;
	}
	return it;
}
